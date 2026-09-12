# 백엔드 컨벤션 안내

작업에 해당하는 문서만 읽습니다. 각 주제의 상세 규칙은 아래 문서가 소유하며, AI 작업 방식과 완료 명령은 [루트 지침](../../AGENTS.md)을 따릅니다.

| 작업 | 문서 |
| --- | --- |
| Java 포맷·이름 | [포맷·명명](code-style.md) |
| 구조 결정·ADR 상태·대체 이력 | [현재 ADR 요약](../adr/README.md), [ADR 작성 규칙](../adr/003-adr-lifecycle.md#작성과-변경-규칙) |
| 모듈·패키지·Domain/Application | [아키텍처](architecture.md) |
| JPA·스키마·프로필·트랜잭션·이벤트 | [영속성·이벤트](persistence-events.md) |
| HTTP·응답·Web DTO | [Web API](web-api.md) |
| API 스키마·Swagger annotation | [OpenAPI](openapi-conventions.md) |
| 테스트 설계·집중 검사·CI | [테스트](testing.md) |
| 작업 유형·이슈·라벨·PR과 AI 작성·리뷰 | [GitHub 작업 가이드](github-workflow.md) |
| 오류·실패 조사와 사례 기록 | [트러블슈팅](../troubleshooting/README.md) |

현재 모듈의 책임과 공개 계약은 [도메인 지도](../domain/README.md), 구조 선택의 이유와 제약은 [기본 ADR](../adr/001-template-architecture.md)과 [Application 분류·auth 예제 ADR](../adr/002-application-contracts-and-auth-example.md)을 확인합니다. 새 프로젝트 적용과 실행 준비는 [온보딩](../onboarding/README.md)에 있습니다.
