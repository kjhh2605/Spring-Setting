# 도메인과 모듈 지도

현재 구현된 예제 모듈의 책임·공개 계약을 찾는 출발점입니다. 대상 모듈만 아래 문서·지침으로 이어서 읽습니다. 제품 모듈 분리·정책 논의에는 [기획 초안](../planning/use-cases.md)을 사용하며, 초안의 5개 비즈니스 모듈은 현재 구현과 구분합니다.

| 모듈 | 소유 책임 | 공개 계약 | 허용 의존성 |
| --- | --- | --- | --- |
| `shared` | 오류 기반, OpenAPI 오류 문서화, 공통 응답 구현, 설정과 보안 | `shared::error` (`BaseCode`, `BusinessException`, `CommonErrorCode`), `shared::openapi` (`ApiErrorCodes`, `ApiErrorCodesGroup`) | 없음 |
| `user` | 사용자 등록, 소셜 계정 연결과 사용자 요약 조회 | `UserLookup`, `UserSummary`, `UserRegistered`, `SocialAccountProvisioning`, `ProvisionedUser` | `shared::error`, `shared::openapi` |
| `auth` | subject 조회 예제와 등록 이벤트 후속 처리 | 현재 없음 | `shared::error`, `shared::openapi`, `user` |

| 작업 대상 | 읽을 문서·추가 지침 | 확인할 내용 |
| --- | --- | --- |
| 사용자 등록·조회·이벤트 | [User](user.md), [user 지침](../../src/main/java/com/example/user/AGENTS.md) | 불변식·공개 정보·발행 시점과 auth에 미치는 영향 |
| subject 조회·이벤트 소비 | [Auth](auth.md), [auth 지침](../../src/main/java/com/example/auth/AGENTS.md) | 예제 한계·소유 모델 변환·커밋 후 처리 |
| 공통 오류·응답·설정 | [Shared 계약](../conventions/architecture.md#shared-공개-계약), [shared 지침](../../src/main/java/com/example/shared/AGENTS.md) | named interface와 내부 구현, 소비 모듈 영향 |

새 모듈을 추가할 때는 비즈니스 책임, 소유 데이터, 공개 동기 계약, 발행/구독 이벤트, 허용 의존성을 이 표와 개별 문서에 기록합니다.

검증할 때는 [집중 검사 표](../conventions/testing.md)를 적용합니다. `user`·`auth`는 구조를 보여주는 예제이며, 새 프로젝트에서는 실제 유스케이스와 데이터 소유권에 맞춰 교체합니다.
