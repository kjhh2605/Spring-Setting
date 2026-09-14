# Auth 모듈

## 책임과 범위

카카오 OpenID Connect 로그인과 서비스 자체 토큰 세션을 소유합니다. 카카오 계정 원본과 사용자 상태는 `user`의 공개 계약으로 조회·생성하고, auth는 카카오 인증 결과를 자체 모델로 변환합니다.

- 카카오 Authorization Code 요청에 `state`, `nonce`, PKCE S256을 적용합니다.
- 카카오 ID Token의 RS256 서명, issuer, audience, 만료, nonce를 검증합니다.
- 15분 Access JWT와 14일 opaque Refresh Token을 발급합니다.
- Redis에서 Refresh Token Rotation, 재사용 탐지, 현재·전체 로그아웃과 사용자당 최대 5세션을 관리합니다.
- 카카오 Access/Refresh Token은 로그인 이후 사용하거나 저장하지 않습니다.

기존 `/api/v1/auth/examples/**` subject 조회와 등록 이벤트 로그는 모듈 경계 예제로 유지합니다. 예제 subject는 인증 증명이 아니며 권한을 부여하지 않습니다.

## 로그인 흐름

```text
POST /api/v1/auth/kakao/authorization-requests
  → state·nonce·code_verifier 생성 및 Redis 5분 저장
  → OIDC·PKCE 카카오 인가 URL 반환

카카오가 프런트 redirect URI로 code·state 전달

POST /api/v1/auth/kakao/login
  → state를 Redis에서 1회 소비
  → code·client secret·code_verifier로 카카오 토큰 요청
  → ID Token 서명·iss·aud·exp·nonce 검증
  → user.SocialAccountProvisioning으로 사용자 조회 또는 생성
  → Access JWT 응답 및 Refresh Token HttpOnly 쿠키 발급
```

카카오 회원번호를 공급자 식별자로 사용합니다. 이메일과 닉네임은 로그인 식별자가 아닙니다. 최초 로그인 사용자는 `PENDING_ONBOARDING`, 기존 일반 등록 사용자는 `ACTIVE`이며 Access JWT의 `onboarding_required` claim과 응답 필드에 가입 완료 필요 여부를 제공합니다.

## 토큰과 RTR

- Access JWT: HS256, `iss`, `sub=user:{id}`, `uid`, `sid`, `jti`, `iat`, `exp`, `onboarding_required` claim. 기본 만료 15분.
- Refresh Token: 32바이트 난수의 Base64URL 문자열. 기본 절대 만료 14일.
- Redis에는 Refresh Token 원문 대신 SHA-256 해시, 사용자·세션·family·만료·상태만 저장합니다.
- 갱신은 Lua script에서 기존 토큰 확인, `USED` 전환, 신규 토큰 저장과 family 포인터 변경을 원자 처리합니다.
- 사용된 토큰이 다시 제출되면 해당 family를 `REVOKED`로 바꾸며 현재 활성 Refresh Token도 사용할 수 없습니다.
- 현재 로그아웃은 한 family, 전체 로그아웃은 사용자의 모든 family를 폐기합니다. 사용자당 5세션을 넘으면 가장 오래된 family를 폐기합니다.

RTR은 동시 refresh 요청 하나만 성공시키므로 클라이언트도 refresh 요청을 single-flight로 직렬화해야 합니다. 로그아웃과 family 폐기는 이미 발급된 Access JWT를 즉시 블랙리스트 처리하지 않으므로 최대 15분간 유효할 수 있습니다.

## 쿠키와 설정

웹 Refresh Token 쿠키는 `HttpOnly`, 운영 `Secure`, 기본 `SameSite=Lax`, 경로 `/api/v1/auth`입니다. 로컬·테스트만 `Secure=false`입니다. 프런트와 API를 cross-site로 배치하여 `SameSite=None`을 사용하면 별도 CSRF 방어를 설계해야 합니다.

JWT 만료와 secret, 카카오 client 정보, 쿠키와 세션 제한은 `application.yml`의 환경변수 참조로 관리합니다. 실제 secret은 저장소에 커밋하거나 로그에 출력하지 않습니다. `JWT_SECRET`은 32바이트 이상의 값을 Base64로 인코딩해야 합니다.

## 실패와 운영 특성

| 코드 | 의미 |
| --- | --- |
| `AUTH-002` | Refresh Token 없음·만료·폐기·알 수 없음 |
| `AUTH-003` | 이미 회전한 Refresh Token 재사용 탐지 |
| `AUTH-004` | 카카오 토큰 교환 또는 ID Token 검증 실패 |
| `AUTH-005` | OAuth state 만료·불일치·재사용 |

Redis 장애 중에는 인가 요청 생성, 로그인 state 소비, refresh, logout을 처리할 수 없습니다. 서명 검증이 가능한 기존 Access JWT 요청은 Redis를 조회하지 않으므로 만료 전까지 계속 처리됩니다.

## 패키지와 공개 계약

- HTTP·쿠키: `adapter/in/web`, OpenAPI 계약은 `adapter/in/web/docs`.
- 카카오 연동: `adapter/out/kakao`.
- Redis state·session: `adapter/out/redis`.
- JWT·난수 생성: `adapter/out/security`.
- 사용자 변환: `adapter/out/user`에서 `user`의 공개 계약을 auth 모델로 변환.
- auth가 다른 모듈에 공개하는 루트 타입은 없습니다.

세부 보안 결정은 [ADR-003](../adr/003-kakao-login-and-token-security.md)을 따릅니다.
