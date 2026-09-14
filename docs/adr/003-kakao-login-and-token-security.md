# ADR-003: 카카오 로그인과 토큰 보안

- 상태: Accepted
- 기준일: 2026-09-14
- 범위: 카카오 OIDC 로그인, 서비스 JWT와 Redis Refresh Token Rotation, 쿠키·폐기·운영 제약

## 배경

웹과 모바일의 카카오 로그인을 단일 백엔드 인증으로 연결하면서 짧은 Access Token과 장기 로그인 세션이 필요합니다. Refresh Token 탈취와 재사용을 탐지하고, 실제 비밀값을 소스에 포함하지 않으며, user와 auth의 모듈 책임을 유지해야 합니다.

## 결정

- 카카오 Authorization Code와 OpenID Connect를 사용합니다. 인가 요청마다 Redis에 5분 TTL의 `state`, `nonce`, PKCE S256 `code_verifier`를 저장하고 callback 처리 시 한 번만 소비합니다.
- 카카오 ID Token은 JWK로 RS256 서명을 검증하고 `iss`, `aud`, `exp`, `nonce`를 확인합니다. 카카오 회원번호 `sub`를 공급자 식별자로 사용하며 이메일은 식별자로 사용하지 않습니다.
- user가 `(provider, provider_user_id)`와 가입 상태를 소유합니다. auth의 user adapter가 공개 `SocialAccountProvisioning` 계약을 호출하고 auth 소유 모델로 변환합니다.
- 서비스 Access Token은 HS256 JWT이며 기본 만료는 15분입니다. Base64로 전달된 32바이트 이상 secret과 만료시간은 YAML의 환경변수 참조로 주입합니다.
- Refresh Token은 32바이트 opaque 난수이고 기본 절대 만료는 14일입니다. Redis에는 SHA-256 해시만 저장합니다.
- refresh마다 새 토큰을 발급하고 이전 토큰을 `USED`로 바꾸며 family의 활성 해시를 교체합니다. Lua script로 이를 원자 처리하고 사용된 토큰 재제출 시 family 전체를 폐기합니다.
- 사용자당 최대 5세션을 허용합니다. 현재 로그아웃은 해당 family, 전체 로그아웃은 모든 family를 폐기합니다.
- 웹 Refresh Token은 `HttpOnly`, 운영 `Secure`, 기본 `SameSite=Lax` 쿠키로 전달합니다. Access Token은 응답 본문으로 전달하고 Bearer 인증에 사용합니다.
- 카카오 Access/Refresh Token은 별도 카카오 API를 사용하지 않으므로 로그인 이후 저장하지 않습니다.

## 영향과 제약

- 로그아웃은 Refresh Token을 폐기하지만 이미 발급된 Access JWT는 최대 15분간 유효합니다. 즉시 폐기가 필요한 정책이 생기면 별도 denylist 또는 세션 버전 검사를 설계해야 합니다.
- Redis 장애 중 OAuth 요청 state와 refresh/logout을 처리할 수 없습니다. 기존 Access JWT 검증은 Redis에 의존하지 않습니다.
- strict rotation이므로 클라이언트는 동시 refresh를 single-flight로 직렬화해야 합니다.
- `SameSite=None`이 필요한 cross-site 배포는 Secure 쿠키와 별도의 CSRF 방어를 함께 설계해야 합니다.
- HS256 secret 교체 시 기존 토큰 검증을 유지하는 다중 키 기간이 없습니다. 무중단 키 회전 요구가 생기면 `kid`와 복수 검증 키 또는 비대칭 키로 확장합니다.
- 운영 사용자 스키마는 Flyway가 관리합니다. 기존 수동 스키마를 도입하는 배포는 baseline 정책을 별도로 결정해야 합니다.

## 검증

- 카카오 URL·토큰 요청·OIDC claim/nonce 경계 단위 테스트
- Redis Testcontainers에서 일회 소비, 원자 회전, 재사용 family 폐기, 로그아웃과 최대 세션 검증
- PostgreSQL Testcontainers에서 Flyway V1과 소셜 계정 고유 매핑 검증
- MockMvc에서 로그인, 쿠키 회전, 재사용 오류, Bearer 보호와 OpenAPI 계약 검증

관련 구조 결정은 [ADR-001](001-backend-architecture.md), 에이전트 작업 규칙은 [ADR-002](002-agentic-coding-rules.md)를 따릅니다.
