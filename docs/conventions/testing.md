# 테스트 설계와 실행

테스트 작성·실행과 CI 변경에 적용합니다. 완료 명령·문서-only 예외는 [루트 지침](../../AGENTS.md#검증과-완료)이 원본입니다. 테스트 코드 수정 시 [테스트 지침](../../src/test/java/com/example/AGENTS.md)과 대상 모듈 지침을 함께 적용합니다.

## 설계

- 새 기능·버그 수정은 기대 행동·재현 조건의 의도한 실패부터 확인합니다. 구현 복제나 문서·포맷만의 변경을 위한 무의미한 테스트는 추가하지 않습니다.
- Domain/Application 규칙·흐름은 Spring Context 없는 단위 테스트, 독립 모듈 조립은 `@ApplicationModuleTest`로 검증합니다.
- DB·영속성·전체 API는 H2 대체 없이 PostgreSQL Testcontainers를 사용합니다. 핵심 API는 MockMvc로 공통 응답·보안·트랜잭션·영속성을 함께 확인합니다.
- 전체 API·이벤트·모듈 통합 테스트에 클래스 수준 `@Transactional`을 붙이지 않습니다. 매핑용 `@DataJpaTest`의 기본 롤백은 허용하지만 커밋·이벤트 검증을 대체하지 않습니다.
- 시간은 고정 `Clock`/`Instant`, 비동기는 제한 시간 있는 조건 기반 대기로 검증합니다. 테스트 데이터는 공유 DB 상태·실행 순서에 의존하지 않도록 격리합니다.
- 리스너 직접 호출·mock은 위임/발행 계약만 검증합니다. 실제 커밋 전 미처리·커밋 후 별도 스레드/트랜잭션·롤백 시 미처리는 이벤트 통합 테스트로 확인합니다.
- 통과를 위해 assertion·아키텍처 규칙·CI 검사를 약화하지 않습니다. 실패를 제품·테스트·환경 문제로 구분합니다.

## 아키텍처와 집중 검사

`./gradlew test --tests '<정확한 클래스명>'`으로 변경 범위를 먼저 검사합니다. 아래 공통 테스트와 모듈 `AGENTS.md`의 관련 테스트를 선택합니다.

| 변경 | 테스트 클래스 |
| --- | --- |
| 모듈 경계·공개 계약 | `com.example.ModularityTest` |
| 내부 의존성·JPA Entity 위치 | `com.example.ArchitectureTest` |
| 등록 트랜잭션·이벤트·비동기 설정 | `com.example.UserRegistrationEventIntegrationTest` |
| HTTP·응답·예외·보안 | `com.example.ApiWorkflowIntegrationTest` |
| OpenAPI 계약 | `com.example.OpenApiDocumentationIntegrationTest` |

- `ModularityTest`의 `ApplicationModules.verify()`는 순환·허용 의존성·내부 패키지 침범을 CI에서 차단합니다.
- `ArchitectureTest`는 Domain의 Spring·Jakarta·Hibernate·QueryDSL 의존, Application의 Adapter·영속 기술 의존, 입력 Adapter의 출력 Port·Persistence·`@Service` 구현 의존 및 JPA Entity 위치를 검사합니다.
- 새 아키텍처 규칙은 의도적인 위반이 실제로 실패하는지 확인한 뒤 위반 코드를 제거합니다.

## 전체 실행과 CI

- 집중 테스트 후 코드·빌드·실행 설정 변경은 루트의 전체 검증을 수행합니다. `--tests` 필터는 완료 검증에 사용하지 않습니다.
- 일반 로컬 `./gradlew test`는 Docker가 없으면 컨테이너 테스트를 건너뜁니다. 전체 실행은 `./gradlew test -PrequireAllTests=true`를 사용합니다.
- `CI=true` 또는 `-PrequireAllTests=true`에서는 건너뛴 테스트가 있으면 실패합니다. `-PrequireAllTests=false`로 CI 정책을 해제할 수 없습니다.
- Docker를 사용할 수 없으면 가능한 검사부터 실행하고 나머지는 원인과 함께 미검증으로 보고합니다. 성공·실패·건너뛰기·필터 여부를 구분하며 이전 실행 결과를 이번 실행으로 보고하지 않습니다.
- 최종 상태의 필수 검사 통과 후 추가 변경·실패·미해결 우려 없이 반복·확대하지 않습니다.
- CI는 Docker를 먼저 확인하고 성공 여부와 관계없이 테스트·Checkstyle·JaCoCo 보고서를 `verification-reports` artifact로 14일간 보관합니다.

선택 배경: [포맷과 검증 결정](../adr/001-template-architecture.md#포맷과-검증).
