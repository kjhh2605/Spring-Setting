# 도메인과 모듈 지도

| 모듈 | 소유 책임 | 공개 계약 | 허용 의존성 |
| --- | --- | --- | --- |
| `shared` | 오류 기반, OpenAPI 오류 문서화, 공통 응답 구현, 설정과 보안 | `shared::error` (`BaseCode`, `BusinessException`, `CommonErrorCode`), `shared::openapi` (`ApiErrorCodes`, `ApiErrorCodesGroup`) | 없음 |
| `user` | 사용자 등록과 사용자 요약 조회 | `UserLookup`, `UserSummary`, `UserRegistered` | `shared::error`, `shared::openapi` |
| `activity` | 사용자 활동 설명과 등록 이벤트 후속 처리 | 현재 없음 | `shared::error`, `shared::openapi`, `user` |

세부 문서:

- [User](user.md)
- [Activity](activity.md)

새 모듈을 추가할 때는 비즈니스 책임, 소유 데이터, 공개 동기 계약, 발행/구독 이벤트, 허용 의존성을 이 표와 개별 문서에 기록합니다.

검증할 때는 [집중 검사 표](../conventions/testing.md#아키텍처와-집중-검사)를 적용합니다. `user`·`activity`는 구조를 보여주는 예제이며, 새 프로젝트에서는 실제 유스케이스와 데이터 소유권에 맞춰 교체합니다.
