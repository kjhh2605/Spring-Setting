# Auth 모듈 지침

[루트 지침](../../../../../../AGENTS.md)에 추가 적용합니다. [Auth 문서](../../../../../../docs/domain/auth.md)를 읽습니다.

- 실제 인증은 검증된 카카오 OIDC 결과로만 시작합니다. 기존 example subject 조회 결과나 사용자 식별자만으로 토큰·세션을 만들지 않습니다.
- 카카오 토큰은 로그인 완료 후 저장하지 않습니다. ID Token의 서명·issuer·audience·만료·nonce 검증을 우회하지 않습니다.
- Refresh Token 원문은 Redis에 저장하지 않고, 회전은 원자 처리하며 사용 토큰 재제출 시 family 전체를 폐기합니다.
- `adapter/out/user/UserSubjectAdapter`에서 user의 공개 `UserLookup` 결과를 auth 소유 `AuthSubject`로 변환합니다. user 내부 타입과 Repository는 참조하지 않습니다.
- `adapter/in/event/UserRegisteredListener`는 공개 이벤트를 auth 소유 Command로 변환하고 입력 Port에 위임합니다. Application과 Domain에서 user 타입을 참조하지 않습니다.
- 등록 커밋 후 별도 스레드·트랜잭션에서 소비하며 롤백 시 처리하지 않습니다. 로그는 `userId`, `occurredAt`만 기록합니다.
- auth는 사용자 원본·별도 테이블·영속 이벤트 저장소를 소유하지 않습니다. 재처리 확장은 [영속성·이벤트](../../../../../../docs/conventions/persistence-events.md)를 따릅니다.

## 집중 검증

- Domain: `com.example.auth.domain.AuthSubjectTest`.
- 조회: `com.example.auth.application.service.GetAuthSubjectServiceTest`.
- 경계 변환: `com.example.auth.adapter.out.user.UserSubjectAdapterTest`, `com.example.auth.adapter.in.event.UserRegisteredListenerTest`.
- 모듈 조립: `com.example.auth.AuthModuleTest`.
- HTTP·OpenAPI·이벤트·모듈 경계는 [공통 검사 표](../../../../../../docs/conventions/testing.md)를 따릅니다. 실제 커밋·롤백·비동기 검증을 리스너 직접 호출로 대체하지 않습니다.
- 로그인·RTR API: `com.example.AuthApiIntegrationTest`.
- Redis state·RTR: `com.example.auth.adapter.out.redis.RedisOAuthLoginRequestStoreTest`, `RedisRefreshSessionStoreTest`.
- 카카오·JWT 경계: `com.example.auth.adapter.out.kakao.*Test`, `com.example.auth.adapter.out.security.JwtAccessTokenIssuerTest`.
