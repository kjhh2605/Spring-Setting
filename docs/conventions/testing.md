# 테스트 설계와 실행

테스트 작성·실행과 CI 변경에 적용합니다. 완료 명령·문서-only 예외는 [루트 지침](../../AGENTS.md)이 원본입니다. 테스트 코드 수정 시 [테스트 지침](../../src/test/java/com/example/AGENTS.md)과 대상 모듈 지침을 함께 적용합니다.

## 설계

- 새 기능·버그 수정은 기대 행동·재현 조건의 의도한 실패부터 확인합니다. 구현 복제나 문서·포맷만의 변경을 위한 무의미한 테스트는 추가하지 않습니다.
- Domain/Application 규칙·흐름은 Spring Context 없는 단위 테스트, 독립 모듈 조립은 `@ApplicationModuleTest`로 검증합니다.
- DB·영속성·전체 API는 H2 대체 없이 PostgreSQL Testcontainers를 사용합니다. 핵심 API는 MockMvc로 공통 응답·보안·트랜잭션·영속성을 함께 확인합니다.
- 전체 API·이벤트·모듈 통합 테스트에 클래스 수준 `@Transactional`을 붙이지 않습니다. 매핑용 `@DataJpaTest`의 기본 롤백은 허용하지만 커밋·이벤트 검증을 대체하지 않습니다.
- 시간은 고정 `Clock`/`Instant`, 비동기는 제한 시간 있는 조건 기반 대기로 검증합니다. 테스트 데이터는 공유 DB 상태·실행 순서에 의존하지 않도록 격리합니다.
- 리스너 직접 호출·mock은 위임/발행 계약만 검증합니다. 실제 커밋 전 미처리·커밋 후 별도 스레드/트랜잭션·롤백 시 미처리는 이벤트 통합 테스트로 확인합니다.
- 통과를 위해 assertion·아키텍처 규칙·CI 검사를 약화하지 않습니다. 실패를 제품·테스트·환경 문제로 구분합니다.
- 계약 불일치로 실패하면 오류가 가리키는 선언·구현·호출부를 함께 비교하고 원인을 수정한 뒤 해당 집중 검사를 재실행합니다. 한쪽 선언만 추측으로 바꾸며 반복 실행하지 않습니다.

## 아키텍처와 집중 검사

`./gradlew test --tests '<정확한 클래스명>'`으로 변경 범위를 먼저 검사합니다. 아래 공통 테스트와 모듈 `AGENTS.md`의 관련 테스트를 선택합니다.

DB·컨테이너 검사는 테스트 전용 자원도 삭제할 수 있으므로 [실행 전 승인 절차](github-workflow.md#승인-절차)를 따릅니다. 전체 `test`뿐 아니라 `build`·`check` 등 간접 실행도 실제 task graph와 테스트 구성을 확인합니다. 클래스 이름·`--tests` 사용만으로 DB 삭제가 없는 검사라고 판단하지 않습니다. DB가 없는 집중 검사는 추가 승인 없이 수행합니다.

| 변경 | 테스트 클래스 |
| --- | --- |
| 모듈 경계·공개 계약 | `com.example.ModularityTest` |
| 내부 의존성·JPA Entity 위치 | `com.example.ArchitectureTest` |
| 등록 트랜잭션·이벤트·비동기 설정 | `com.example.UserRegistrationEventIntegrationTest` |
| HTTP·응답·예외·보안 | `com.example.ApiWorkflowIntegrationTest` |
| 공통 Security·Bean 조립과 소비 모듈 의존성 | 영향받는 `com.example.user.UserModuleTest`, `com.example.auth.AuthModuleTest`와 위 API 검사 |
| OpenAPI 계약 | `com.example.OpenApiDocumentationIntegrationTest` |

- 공통 설정의 Bean 의존성이 바뀌면 소비 모듈을 격리한 조립 검사도 같은 변경 단위에 포함합니다. 전체 애플리케이션에서만 존재하는 Bean 때문에 실패할 수 있으므로 API 검사나 정적 의존 검사만으로 대체하지 않습니다.
- [ModularityTest](../../src/test/java/com/example/ModularityTest.java)의 `ApplicationModules.verify()`는 순환·허용 의존성·내부 패키지 침범을 CI에서 차단합니다.
- [ArchitectureTest](../../src/test/java/com/example/ArchitectureTest.java)는 Domain의 Application·Adapter·Spring·Jakarta·Hibernate·QueryDSL 의존, Application의 Adapter·영속 기술 의존, 입력 Adapter의 출력 Port·Persistence·`@Service` 구현 의존 및 JPA Entity 위치를 검사합니다.
- 새 아키텍처 규칙은 의도적인 위반이 실제로 실패하는지 확인한 뒤 위반 코드를 제거합니다.

## 검사 실행과 출력

- DB·컨테이너 검사는 승인 범위를 확인한 뒤 Docker 가용성을 먼저 조회합니다. 실행하지 못하는 검사를 반복 호출하지 않고 가능한 독립 검사부터 수행합니다.
- 같은 변경 단위에서 필요한 테스트 클래스는 한 Gradle 호출의 여러 `--tests`로 묶을 수 있습니다. 의도한 실패 확인과 수정 후 검증은 구분하며 새 변경·실패 원인이 있을 때 필요한 검사를 다시 실행합니다.
- 진행 상황을 즉시 볼 필요가 없으면 실행 도구의 초기·후속 대기를 10~30초로 잡아 같은 프로세스를 짧게 반복 조회하지 않습니다. 시간 초과·중단 대응과 사용자 진행 안내가 필요하면 적절히 나눕니다. 검사 자체의 timeout이나 실행 범위는 줄이지 않습니다.
- 성공 시 종료 코드·검사 수·성공/실패/건너뛰기 요약을 확인합니다. 큰 로그는 임시 파일·기존 보고서에 보존하고 실패 원인과 관련 구간만 읽습니다. 출력 축약으로 실패·미실행을 숨기거나 실행 도구의 오류를 무시하지 않습니다.

## 전체 실행과 CI

- 코드 변경의 중간 커밋은 [단위별 집중 검사·계약 검토](github-workflow.md#작업-중-커밋-체크포인트) 후 생성합니다. 모든 단위의 검토와 알려진 수정 사항을 마친 뒤 루트의 전체 검증을 수행하며, 새로 발견한 문제의 수정·재검증은 생략하지 않습니다. `--tests` 필터는 완료 검증에 사용하지 않습니다.
- 일반 로컬 `./gradlew test`는 Docker가 없으면 컨테이너 테스트를 건너뜁니다. 전체 실행은 `./gradlew test -PrequireAllTests=true`를 사용합니다.
- `CI=true` 또는 `-PrequireAllTests=true`에서는 0건 실행 또는 건너뛴 테스트가 있으면 실패합니다. `-PrequireAllTests=false`로 CI 정책을 해제할 수 없습니다. 이 옵션이 `--tests` 필터 자체를 감지하지는 않으므로, 전체 실행 여부는 실제 명령도 확인합니다.
- Docker를 사용할 수 없으면 가능한 검사부터 실행하고 나머지는 원인과 함께 미검증으로 보고합니다. 성공·실패·건너뛰기·필터 여부를 구분하며 이전 실행 결과를 이번 실행으로 보고하지 않습니다.
- 최종 상태의 필수 검사 통과 후 추가 변경·실패·미해결 우려 없이 반복·확대하지 않습니다.
- CI는 Docker를 먼저 확인하고 성공 여부와 관계없이 테스트·Checkstyle·JaCoCo 보고서를 `verification-reports` artifact로 14일간 보관합니다.
- 위 승인 절차는 에이전트가 로컬에서 검사를 실행할 때 적용합니다. 기존 CI의 실행 조건·테스트 정리 동작은 변경하지 않습니다. 에이전트가 푸시 등으로 CI 실행을 유발할 경우 승인 요약에 CI의 DB 테스트·자원 정리도 포함합니다.

## 자동 검사와 리뷰의 역할

| 대상 | 기존 검사·설정 | 남는 판단 |
| --- | --- | --- |
| 포맷·import·명명 형태 | [Spotless](../../build.gradle.kts), [Checkstyle 설정](../../gradle/quality.gradle.kts)과 [규칙](../../config/checkstyle/checkstyle.xml) | `get`/`find`의 의미, DTO 소유권은 [컨벤션](code-style.md)과 리뷰로 확인 |
| 모듈·계층 경계 | 위 `ModularityTest`, `ArchitectureTest` | 비즈니스 책임·공개 정보의 적절성, 새 규칙의 검사 필요성 |
| HTTP·OpenAPI·이벤트 | 위 집중 검사 표의 통합 테스트 | 작성된 시나리오를 검증하며 새 API·정책까지 자동 보장하지 않음 |
| 프로필 | [ApplicationProfileConfigurationTest](../../src/test/java/com/example/shared/internal/config/ApplicationProfileConfigurationTest.java) | 실제 배포 환경의 인증·접근·마이그레이션 정책 |
| 전체 테스트·보고서 | [테스트 설정](../../gradle/testing.gradle.kts), [CI](../../.github/workflows/ci.yml) | 필터 없는 실행 여부; JaCoCo는 보고서 생성이며 최소 커버리지 게이트는 없음 |
| Markdown·ADR·PR 작성 | [문서 검증 기준](../../AGENTS.md#검증과-완료), [ADR 관리 규칙](../adr/002-agentic-coding-rules.md#문서와-adr-관리), [GitHub 가이드](github-workflow.md) | 현재 Markdown 링크·ADR 상태·PR 제목/라벨을 검사하는 저장소 CI는 없음 |

선택 배경: [포맷과 검증 결정](../adr/001-backend-architecture.md#포맷과-검증).
