# ADR-001: 백엔드 템플릿 아키텍처

- 상태: Partially Superseded
- 기준일: 2026-09-08
- 상태 변경일: 2026-09-12
- 후속 결정: [ADR-002](002-application-contracts-and-auth-example.md), [ADR-003](003-adr-lifecycle.md)

## 목적과 범위

새 Java 백엔드 프로젝트가 실행·모듈 경계·검증 체계를 갖춘 상태에서 시작하도록 합니다. ~~이 문서는 템플릿의 현재 결정과 이유를 담는 단일 ADR입니다.~~ ([ADR-003로 대체](003-adr-lifecycle.md)) 세팅 과정의 계획·실험 기록은 포함하지 않으며, ~~템플릿의 구조나 정책을 변경할 때 이 문서와 해당 상세 규칙을 함께 갱신합니다.~~ ([ADR-003로 대체](003-adr-lifecycle.md))

## 배포와 모듈 경계

- Java 21과 Spring Boot·Spring Modulith를 사용하고 Gradle의 단일 실행 JAR로 배포합니다. 버전은 `build.gradle.kts`, `gradle/libs.versions.toml`, Gradle Wrapper에서 관리합니다.
- 하나의 배포 단위 안에서 비즈니스 책임별 패키지를 Application Module로 구분합니다. ~~현재 `user`·`activity`는 동기 조회와 이벤트 연동을 보여주는 예제이고,~~ ([ADR-002로 대체](002-application-contracts-and-auth-example.md)) `shared`는 공통 기술 계약과 인프라를 담당합니다.
- 비즈니스 모듈의 공개 계약은 모듈 루트에 둡니다. `shared`는 오류·OpenAPI 계약을 named interface로 나누어 소비자가 필요한 계약만 허용하도록 합니다. 내부 패키지 공개로 검증을 우회하지 않습니다.
- 초기에는 별도 Gradle 모듈이나 서비스로 나누지 않습니다. 배포·운영 비용을 낮추는 대신 모듈 경계와 순환 의존성을 `ApplicationModules.verify()`로 검증합니다.

모듈별 책임·공개 타입·허용 의존성은 [도메인 지도](../domain/README.md), 패키지와 수정 위치는 [아키텍처 규칙](../conventions/architecture.md)이 원본입니다.

## 모듈 내부와 HTTP 계약

- Domain, Application Port, Adapter를 분리합니다. Domain은 기술에 의존하지 않고, Application은 Port를 통해 외부 기술을 사용합니다. 입력 Adapter는 입력 Port에 위임합니다.
- Domain 모델과 JPA Entity, Web DTO를 분리합니다. 변환 코드가 생기더라도 영속성과 외부 API 변경이 비즈니스 모델이나 다른 모듈로 전파되지 않도록 합니다.
- 같은 대상도 모듈별 정보·의미·규칙이 다르면 소비 모듈이 자체 Domain 모델을 소유합니다. 공개 DTO·이벤트는 전달 계약으로 유지하고 Port·Adapter 경계에서 변환하여 제공 모듈의 모델 변경이 소비 모듈의 규칙으로 전파되는 것을 줄입니다. 단순 표시·조회에는 별도 Domain 모델을 강제하지 않으며, 모델 분리는 테이블 복제나 원본 데이터 소유권 이전을 뜻하지 않습니다. 구체적인 적용 기준과 ~~현재 activity 예제의 범위~~ ([ADR-002로 대체](002-application-contracts-and-auth-example.md))는 [아키텍처 규칙](../conventions/architecture.md#소비-모듈의-모델과-외부-정보-변환)에 둡니다.
- HTTP 응답·오류 처리와 OpenAPI 래퍼 생성은 공통 인프라가 담당합니다. 각 모듈의 ControllerDocs는 실제 결과 타입과 오류 계약을 선언하여 런타임 응답과 문서의 중복 작성을 줄입니다.
- 모듈 전용 오류는 `application.error`가 소유하고 `CommonErrorCode`에는 공통 오류만 둡니다. `BusinessException(BaseCode)`와 전역 처리 방식을 유지하며, 응답·OpenAPI 매핑 중복을 줄이기 위해 Application 오류에는 HTTP 상태를 허용합니다. Domain 불변식 오류에는 이 웹 지향 계약을 사용하지 않습니다.
- 신규 오류 식별자는 HTTP 상태와 독립적인 모듈별 일련번호를 사용합니다. 상태 변경에도 클라이언트의 오류 식별을 안정적으로 유지하며, 기존 응답 코드는 호환성을 위해 보존합니다. 상세 규칙은 [아키텍처 규칙](../conventions/architecture.md)에 둡니다.

세부 계약은 [Web API](../conventions/web-api.md)와 [OpenAPI](../conventions/openapi-conventions.md)를 따릅니다.

## 영속성과 이벤트

- PostgreSQL·JPA·QueryDSL을 사용하고 DB 검증은 같은 제품의 Testcontainers로 수행합니다. H2로 대체하지 않아 SQL·매핑 차이를 실제 DB에서 확인합니다.
- 공통·운영 설정은 스키마를 자동 변경하지 않습니다. 로컬 예제·테스트만 임시 스키마를 사용합니다. 활성 프로필과 운영 DB 접속 정보는 실행 환경에서 지정합니다.
- 즉시 결과가 필요한 모듈 간 조회는 공개 API로 호출하고, 등록 완료 후속 처리는 커밋 후 비동기 이벤트로 실행합니다. 시간은 주입받은 Clock과 UTC Instant를 사용합니다.
- 현재 이벤트 소비는 로그 기록이며 영속 Event Publication Registry·Outbox·자동 재처리를 제공하지 않습니다. 처리 실패를 복구해야 하는 서비스는 저장소, 재처리, 멱등성 정책을 함께 설계해야 합니다.
- 마이그레이션 도구와 실제 인증 수단은 제품 요구에 따라 결정합니다. 운영 적용 전에 스키마 준비와 API·Actuator 접근 정책을 구성해야 합니다.

프로필·트랜잭션·이벤트의 실행 규칙은 [영속성·이벤트](../conventions/persistence-events.md)가 원본입니다. 실행 준비는 [온보딩](../onboarding/README.md)을 따릅니다.

## 포맷과 검증

- Java 레이아웃은 Spotless의 Palantir Java Format으로 통일합니다. 수동 줄바꿈 조정과 formatter 간 차이를 줄이고 Checkstyle로 별도의 명명·코드 규칙을 검사합니다.
- 모듈 간 경계는 ModularityTest, 내부 의존성과 JPA Entity 위치는 ArchitectureTest로 검증합니다. API·이벤트 통합 테스트는 실제 응답과 커밋·롤백·비동기 실행 경계를 확인합니다.
- CI는 Docker와 필터 없는 전체 테스트를 요구하고 건너뛰기를 실패로 처리합니다. 로컬 집중 검사는 빠른 피드백에 사용하며 전체 검증을 대체하지 않습니다.

포맷 규칙은 [포맷·명명](../conventions/code-style.md), 테스트 설계와 CI 정책은 [테스트](../conventions/testing.md), 완료 명령은 [AGENTS.md](../../AGENTS.md)가 원본입니다.

## 문서와 작업 지침

- README는 소개·빠른 시작, 온보딩은 새 프로젝트 적용·문제 해결, 컨벤션은 주제별 규칙, 도메인 문서는 현재 모듈 계약을 설명합니다. ADR은 선택 이유와 제약을 기록합니다.
- [트러블슈팅](../troubleshooting/README.md)은 반복 조사에서 얻은 재사용 가능한 문제 해결 지식을 사례별로 보관합니다. 기록 조건은 루트 AGENTS.md, 작성 규칙과 색인은 트러블슈팅 README에서 관리하며, 세팅 과정의 실행 이력은 누적하지 않습니다.
- AI 공통 작업 규칙은 루트 AGENTS.md에, 모듈·테스트의 추가 규칙은 해당 경로에 둡니다. 도구별 지침은 루트 문서의 포인터로 유지합니다.
- 작업에 해당하는 주제와 모듈 문서만 읽습니다. 모델이나 플러그인에 종속된 실행 절차를 저장소의 필수 작업 방식으로 두지 않습니다.
- 작업 유형은 변경 목적에 따라 라벨로 세분화하고, 이슈 Form은 필요한 입력이 같은 유형끼리 공유합니다. PR은 공통 양식과 유형을 표시한 제목을 사용합니다. 독립된 목적은 PR을 분리하고, 큰 변경의 AI 리뷰는 의미 단위로 나눈 뒤 전체 연결 관계를 확인합니다. 분류의 정확도를 유지하면서 양식과 사람·AI 작성·리뷰 규칙의 중복을 줄입니다. 상세 기준과 적용 방법은 [GitHub 작업 가이드](../conventions/github-workflow.md)에서 관리합니다.

새 프로젝트 적용 순서는 [온보딩](../onboarding/README.md), 문서 탐색은 [컨벤션 목차](../conventions/backend-conventions.md)를 사용합니다.
