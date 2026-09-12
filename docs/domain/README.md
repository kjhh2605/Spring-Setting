# 도메인과 모듈 지도

| 모듈 | 소유 책임 | 공개 계약 | 허용 의존성 |
| --- | --- | --- | --- |
| `shared` | 오류 기반, OpenAPI 오류 문서화, 공통 응답 구현, 설정과 보안 | `shared::error` (`BaseCode`, `BusinessException`, `CommonErrorCode`), `shared::openapi` (`ApiErrorCodes`, `ApiErrorCodesGroup`) | 없음 |
| `user` | 사용자 등록·소셜 계정 연결·요약 조회 | `UserLookup`, `UserSummary`, `UserRegistered`, `SocialUserRegistration` | `shared::error`, `shared::openapi` |
| `auth` | 카카오 로그인·JWT·Redis RTR·dev 발급과 기존 예제 | 현재 없음 | `shared::error`, `shared::openapi`, `user` |

세부 문서:

- [User](user.md)
- [Auth](auth.md)

새 모듈을 추가할 때는 비즈니스 책임, 소유 데이터, 공개 동기 계약, 발행/구독 이벤트, 허용 의존성을 이 표와 개별 문서에 기록합니다.

검증할 때는 [집중 검사 표](../conventions/testing.md)를 적용합니다. 기존 등록·조회 예제와 실제 소셜 로그인 기능은 각 모듈 문서에서 구분합니다.
