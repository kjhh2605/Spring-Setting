# 백엔드 컨벤션 안내

먼저 [대상 경로의 추가 지침](../domain/README.md#작업-경로와-추가-지침)을 확인하고 아래에서 현재 단계에 필요한 절만 선택합니다. 각 주제의 상세 규칙은 연결된 원본이 소유하며, AI 작업 방식과 완료 명령은 [루트 지침](../../AGENTS.md)이 원본입니다. 이미 읽은 원본으로 되돌아가는 링크는 재조회 요청이 아닙니다.

| 작업 | 읽을 원본 | 확인할 내용 |
| --- | --- | --- |
| Java 포맷·이름 | [포맷·명명](code-style.md) | formatter에 맡길 배치와 의미에 따른 이름 |
| 구조·작업 규칙 결정과 ADR 관리 | [현재 ADR 요약](../adr/README.md), [ADR 관리 규칙](../adr/002-agentic-coding-rules.md#문서와-adr-관리) | 주제별 현재 결정과 선택 이유, Git 이력의 과거 근거 |
| 모듈·패키지·Domain/Application | [아키텍처](architecture.md), [도메인 지도](../domain/README.md)의 해당 모듈 | 의존 방향·수정 위치와 모듈별 책임·계약·추가 지침 |
| JPA·스키마·프로필·트랜잭션·이벤트 | [영속성·이벤트](persistence-events.md) | 저장·발행·소비 경계와 운영 제약 |
| HTTP·응답·Web DTO | [Web API](web-api.md) | 입력 검증·오류 변환·DTO 소유권 |
| API 스키마·Swagger annotation | [OpenAPI](openapi-conventions.md) | ControllerDocs와 실제 응답의 일치 |
| 테스트 설계·집중 검사·CI | [테스트](testing.md) | 변경별 검사 선택과 자동 검증의 범위 |
| 커밋·브랜치·승인 | [커밋](github-workflow.md#커밋), 확인 대상 행위의 [승인 절차](github-workflow.md#승인-절차) | 변경 단위·검증·승인 요약 |
| 이슈·PR 작성과 리뷰 | 요청한 작업의 [이슈](github-workflow.md#이슈-작성)·[PR](github-workflow.md#pr-작성)·[리뷰](github-workflow.md#pr-크기와-ai-리뷰) 절 | 필요한 양식·라벨만 추가 확인; 구현 시작 시 일괄 로딩하지 않음 |
| 오류·실패 조사와 사례 기록 | [트러블슈팅](../troubleshooting/README.md) | 관련 증상과 재사용 가능한 발견의 기록 조건 |
| 실행·새 프로젝트 적용 | [README](../../README.md), [온보딩](../onboarding/README.md) | 실행 명령·예제 API와 식별자 교체 순서 |
| 에이전트 지침·스킬 탐색 점검 | [에이전트 문서 탐색](../onboarding/README.md#에이전트-문서-탐색) | 자동 로딩과 별도 읽기, 공식 동작과 저장소 적용의 구분 |
| Codex 역할·실행 정책·협업 설정 | [Codex 협업 설정](../onboarding/README.md#codex-협업-설정) | 검토 역할 선택·인계, `.rules`와 컨벤션의 역할 구분 |
| 제품 유즈케이스·정책 검토 | [기획 초안](../planning/use-cases.md)의 해당 유즈케이스·미결정 항목 | 관련 제목·키워드로 범위를 좁혀 읽고 무관한 업무 정책은 제외 |

## 구현·결정·초안 구분

- 현재 구현의 진입점은 [도메인 지도](../domain/README.md)입니다. 상세 공개 계약·흐름·미구현 범위는 모듈 문서에서 코드·테스트와 대조하며 이 목차에 복제하지 않습니다. 빌드·버전 값은 [빌드 설정](../../build.gradle.kts)과 [버전 카탈로그](../../gradle/libs.versions.toml)를 확인합니다.
- 확정된 설계와 이유는 [현재 ADR 요약](../adr/README.md)에서 유효한 원문으로 이동합니다. 현재 규칙은 위 주제별 컨벤션에서 찾습니다.
- [기획 초안](../planning/use-cases.md)의 제품 모듈·정책은 구현 사실이나 확정 규칙이 아닙니다. 구현과 문서가 다르면 단순 동기화 누락인지 미결정 정책인지 먼저 구분합니다.

## 대표 작업의 읽기와 검증 경로

아래는 탐색 예시이며 새 기능 구현을 요구하는 목록이 아닙니다. 집중 검사는 [테스트 표](testing.md#아키텍처와-집중-검사), 완료 검사는 [루트 검증 기준](../../AGENTS.md#검증과-완료)을 적용합니다.

| 작업 | 읽기 순서와 판단 기준 | 집중 검증 예시 |
| --- | --- | --- |
| 사용자 등록 불변식·이벤트 수정 | [도메인 지도](../domain/README.md) → User 문서·지침, [아키텍처](architecture.md), [영속성·이벤트](persistence-events.md), [포맷·명명](code-style.md). 공개 이벤트 변경이면 Auth 소비 경계도 확인 | `./gradlew test --tests com.example.user.domain.UserTest --tests com.example.user.application.service.RegisterUserServiceTest --tests com.example.UserRegistrationEventIntegrationTest -PrequireAllTests=true` |
| auth 조회 API·문서 수정 | [도메인 지도](../domain/README.md) → Auth 문서·지침, [Web API](web-api.md), [OpenAPI](openapi-conventions.md), [포맷·명명](code-style.md). subject 예제에 인증 의미를 부여하지 않음 | `./gradlew test --tests com.example.ApiWorkflowIntegrationTest --tests com.example.OpenApiDocumentationIntegrationTest -PrequireAllTests=true` |
| 제품 모듈 분리안 검토·문서 정리 | [기획 초안](../planning/use-cases.md) → [도메인 지도](../domain/README.md), [아키텍처](architecture.md), [현재 ADR 요약](../adr/README.md). 초안을 확정 정책으로 취급하지 않고, 새 결정이 있으면 ADR 작성 규칙 적용 | 실행·빌드·설정 영향이 없는 문서는 링크·경로·일관성 확인과 `git diff --check` |

테스트 코드를 수정하는 경우 [테스트 하위 지침](../../src/test/java/com/example/AGENTS.md)도 읽습니다. 예시의 `--tests` 명령은 전체 테스트 통과를 뜻하지 않습니다.
