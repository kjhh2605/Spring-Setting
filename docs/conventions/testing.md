# 테스트 설계와 실행

테스트 작성·실행과 CI 변경에 적용합니다. 완료 명령·문서-only 예외는 [루트 지침](../../AGENTS.md)이 원본입니다. 테스트 코드 수정 시 [테스트 지침](../../src/test/java/com/example/AGENTS.md)과 대상 모듈 지침을 함께 적용합니다.

## 설계

- 새 기능·버그 수정은 기대 행동·재현 조건의 의도한 실패부터 확인합니다. 구현 복제나 문서·포맷만의 변경을 위한 무의미한 테스트는 추가하지 않습니다.
- 구현 전에 이번 변경의 정상·거부·경계 조건과 각각을 확인할 검사 수단을 정합니다. 테스트 수나 mock 호출 횟수만으로 기능 완성을 판단하지 않습니다. 거부·철회 검사는 준비한 토큰·세션·데이터가 해당 조건 외에는 유효한지 확인하여 잘못된 준비 때문에 통과하지 않게 합니다.
- Domain/Application 규칙·흐름은 Spring Context 없는 단위 테스트, 독립 모듈 조립은 `@ApplicationModuleTest`로 검증합니다.
- DB·영속성·전체 API는 H2 대체 없이 PostgreSQL Testcontainers를 사용합니다. 핵심 API는 MockMvc로 공통 응답·보안·트랜잭션·영속성을 함께 확인합니다.
- 전체 API·이벤트·모듈 통합 테스트에 클래스 수준 `@Transactional`을 붙이지 않습니다. 매핑용 `@DataJpaTest`의 기본 롤백은 허용하지만 커밋·이벤트 검증을 대체하지 않습니다.
- 애플리케이션 내부 시간은 고정 `Clock`/`Instant`로 검증합니다. 실제 저장소의 TTL처럼 외부 시계를 사용하는 검사는 현재 시각을 기준으로 두 시계를 맞추고, 고정된 달력 날짜로 실시간 만료 키를 생성하지 않습니다. 비동기는 제한 시간 있는 조건 기반 대기로 검증하며 데이터는 공유 상태·실행 순서와 격리합니다.
- 리스너 직접 호출·mock은 위임/발행 계약만 검증합니다. 실제 커밋 전 미처리·커밋 후 별도 스레드/트랜잭션·롤백 시 미처리는 이벤트 통합 테스트로 확인합니다.
- 저장소 원자 연산·TTL은 실제 저장소 통합 검사로, 인증 토큰의 claim 검증은 실제 검증기로 확인합니다. 외부 제공자는 응답을 고정할 수 있지만 대체한 경계를 명시하며 실제 제공자 E2E로 보고하지 않습니다.
- 통과를 위해 assertion·아키텍처 규칙·CI 검사를 약화하지 않습니다. 실패를 제품·테스트·환경 문제로 구분합니다.
- 계약 불일치로 실패하면 오류가 가리키는 선언·구현·호출부를 함께 비교하고 원인을 수정한 뒤 해당 집중 검사를 재실행합니다. 한쪽 선언만 추측으로 바꾸며 반복 실행하지 않습니다.

## 아키텍처와 집중 검사

`./gradlew test --tests '<정확한 클래스명>'`으로 변경 범위를 먼저 검사합니다. 아래 공통 테스트와 모듈 `AGENTS.md`의 관련 테스트를 선택합니다.

DB·컨테이너 검사는 테스트 전용 자원도 삭제할 수 있으므로 [실행 전 승인 절차](approvals.md#승인-절차)를 따릅니다. 전체 `test`뿐 아니라 `build`·`check` 등 간접 실행도 실제 task graph와 테스트 구성을 확인합니다. 클래스 이름·`--tests` 사용만으로 DB 삭제가 없는 검사라고 판단하지 않습니다. DB가 없는 집중 검사는 추가 승인 없이 수행합니다.

| 변경 | 테스트 클래스 |
| --- | --- |
| 모듈 경계·공개 계약 | `com.example.ModularityTest` |
| 내부 의존성·JPA Entity 위치 | `com.example.ArchitectureTest` |
| 등록 트랜잭션·이벤트·비동기 설정 | `com.example.UserRegistrationEventIntegrationTest` |
| HTTP·응답·예외·보안 | `com.example.ApiWorkflowIntegrationTest` |
| Bean 등록·생성자 주입·공통 Security·설정과 소비 모듈 의존성 | 영향받는 `com.example.user.UserModuleTest`, `com.example.auth.AuthModuleTest`와 위 API 검사 |
| OpenAPI 계약 | `com.example.OpenApiDocumentationIntegrationTest` |

- `@Service`·`@Bean` 추가, 생성자 주입·프로필·공통 설정이 바뀌면 해당 모듈과 영향받는 소비 모듈의 조립 검사를 같은 커밋 단위에 포함합니다. mock을 주입한 서비스 검사나 전체 API·정적 의존 검사만으로 실제 Bean의 존재와 생성 가능성을 보증하지 않습니다.
- 새 등록 진입점도 기존 이벤트·트랜잭션 계약을 검사합니다. 환경값·스키마 변경은 `.env.example`·실행 안내·운영 적용 자산과 복구 제약을 대조합니다. 테스트 프로필·공통 fixture를 바꿀 때는 기존 격리 설정을 삭제하는 이유와 영향을 확인합니다.
- [ModularityTest](../../src/test/java/com/example/ModularityTest.java)의 `ApplicationModules.verify()`는 순환·허용 의존성·내부 패키지 침범을 CI에서 차단합니다.
- [ArchitectureTest](../../src/test/java/com/example/ArchitectureTest.java)는 Domain의 Application·Adapter·Spring·Jakarta·Hibernate·QueryDSL 의존, Application의 Adapter·영속 기술 의존, 입력 Adapter의 출력 Port·Persistence·`@Service` 구현 의존 및 JPA Entity 위치를 검사합니다.
- 새 아키텍처 규칙은 의도적인 위반이 실제로 실패하는지 확인한 뒤 위반 코드를 제거합니다.

## 검사 실행과 출력

- DB·컨테이너 검사는 승인 범위를 확인한 뒤 Docker 가용성을 먼저 조회합니다. 실행하지 못하는 검사를 반복 호출하지 않고 가능한 독립 검사부터 수행합니다.
- 같은 변경 단위에서 필요한 테스트 클래스는 한 Gradle 호출의 여러 `--tests`로 묶을 수 있습니다. 의도한 실패 확인과 수정 후 검증은 구분하며 새 변경·실패 원인이 있을 때 필요한 검사를 다시 실행합니다.
- 진행 상황을 즉시 볼 필요가 없으면 실행 도구의 초기·후속 대기를 10~30초로 잡아 같은 프로세스를 짧게 반복 조회하지 않습니다. 시간 초과·중단 대응과 사용자 진행 안내가 필요하면 적절히 나눕니다. 검사 자체의 timeout이나 실행 범위는 줄이지 않습니다.
- 성공 시 종료 코드·검사 수·성공/실패/건너뛰기 요약을 확인하고 대상 커밋 또는 커밋할 diff와 연결합니다. 큰 로그는 임시 파일·기존 보고서에 보존하고 실패 원인과 관련 구간만 읽습니다. 출력 축약으로 실패·미실행을 숨기거나 실행 도구의 오류를 무시하지 않습니다.

## 전체 실행과 CI

- 코드 변경의 중간 커밋은 [단위별 집중 검사·계약 검토](commits.md#작업-중-커밋-체크포인트) 후 생성합니다. 각 PR의 변경·알려진 수정을 마치면 루트의 전체 검증을 수행합니다. 커밋마다 전체 검사를 반복할 필요는 없지만 필요한 집중 검사를 생략하지 않습니다. `--tests` 필터는 완료 검증에 사용하지 않습니다.
- 현재 [CI](../../.github/workflows/ci.yml)는 PR의 최종 병합 결과를 검사하며 내부의 모든 커밋을 순회하지 않습니다. 커밋별 조립·실행 가능성은 해당 상태의 검사 근거로 확인합니다. [GitHub의 PR 이벤트 동작](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#pull_request)
- 일반 로컬 `./gradlew test`는 Docker가 없으면 컨테이너 테스트를 건너뜁니다. 전체 실행은 `./gradlew test -PrequireAllTests=true`를 사용합니다.
- `CI=true` 또는 `-PrequireAllTests=true`에서는 0건 실행 또는 건너뛴 테스트가 있으면 실패합니다. `-PrequireAllTests=false`로 CI 정책을 해제할 수 없습니다. 이 옵션이 `--tests` 필터 자체를 감지하지는 않으므로, 전체 실행 여부는 실제 명령도 확인합니다.
- Docker를 사용할 수 없으면 가능한 검사부터 실행하고 나머지는 원인과 함께 미검증으로 보고합니다. 성공·실패·건너뛰기·필터 여부를 구분하며 이전 실행 결과를 이번 실행으로 보고하지 않습니다.
- 최종 상태의 필수 검사 통과 후 추가 변경·실패·미해결 우려 없이 반복·확대하지 않습니다.
- CI는 Docker를 먼저 확인하고 성공 여부와 관계없이 테스트·Checkstyle·JaCoCo 보고서를 `verification-reports` artifact로 14일간 보관합니다.
- [실행 전 승인 절차](approvals.md#승인-절차)는 에이전트가 로컬에서 검사를 실행할 때 적용합니다. 기존 CI의 실행 조건·테스트 정리 동작은 변경하지 않습니다. 에이전트가 푸시 등으로 CI 실행을 유발할 경우 승인 요약에 CI의 DB 테스트·자원 정리도 포함합니다.

## 자동 검사와 리뷰의 역할

- 포맷·구문·경로/앵커 존재·집계처럼 기계적으로 판정 가능한 항목은 기존 명령이나 필요한 범위의 스크립트로 확인합니다. LLM의 추정·수작업 계산을 통과 근거로 쓰지 않습니다.
- 실제 통과 결과의 명령·옵션·대상 SHA 또는 현재 diff·관련 설정과 환경이 현재 검사 대상과 일치하면 재사용합니다. 필터·건너뛰기·검출 범위를 넘어 결과를 일반화하지 않습니다. 실행 가능한 명령이 있다는 사실만으로 검증 완료로 보지 않습니다.
- 유효한 검사 결과가 판정한 동일 조건은 원문을 다시 세거나 다른 에이전트에 반복 판정시키지 않습니다. 위임할 때는 완료한 검사 근거와 미검증 범위를 함께 전달합니다.
- LLM은 요구사항 충족·계약의 의미·누락 시나리오·검사 자체의 적절성을 검토합니다. 테스트·assertion·검사 규칙을 바꾼 경우 통과 결과만으로 검사의 타당성을 보장하지 않습니다.
- 새 변경·실패·검사 범위나 관련 환경 변화·구체적인 검사 신뢰성 우려가 있으면 영향을 받는 범위를 다시 확인합니다. 필수 집중·전체 검사, DB 승인과 커밋 직전 상태 확인에는 기존 기준을 적용합니다.

| 대상 | 기존 검사·설정 | 남는 판단 |
| --- | --- | --- |
| 포맷·import·명명 형태 | [Spotless](../../build.gradle.kts), [Checkstyle 설정](../../gradle/quality.gradle.kts)과 [규칙](../../config/checkstyle/checkstyle.xml) | `get`/`find`의 의미, DTO 소유권은 [컨벤션](code-style.md)과 리뷰로 확인 |
| 모듈·계층 경계 | [집중 검사 표](#아키텍처와-집중-검사)의 `ModularityTest`, `ArchitectureTest` | 비즈니스 책임·공개 정보의 적절성, 새 규칙의 검사 필요성 |
| HTTP·OpenAPI·이벤트 | [집중 검사 표](#아키텍처와-집중-검사)의 통합 테스트 | 작성된 시나리오를 검증하며 새 API·정책까지 자동 보장하지 않음 |
| 프로필 | [ApplicationProfileConfigurationTest](../../src/test/java/com/example/shared/internal/config/ApplicationProfileConfigurationTest.java) | 실제 배포 환경의 인증·접근·마이그레이션 정책 |
| 전체 테스트·보고서 | [테스트 설정](../../gradle/testing.gradle.kts), [CI](../../.github/workflows/ci.yml) | 필터 없는 실행 여부; JaCoCo는 보고서 생성이며 최소 커버리지 게이트는 없음 |
| Markdown·ADR·PR 작성 | [문서 검증 기준](../../AGENTS.md#검증과-완료), [ADR 관리 규칙](../adr/002-agentic-coding-rules.md#문서와-adr-관리), [GitHub 가이드](github-workflow.md) | 현재 링크·ADR 상태·PR 제목/라벨·크기·커밋별 완결성을 자동 차단하는 저장소 CI는 없음 |

선택 배경: [포맷과 검증 결정](../adr/001-backend-architecture.md#포맷과-검증).
