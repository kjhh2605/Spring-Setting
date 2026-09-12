# Auth 모듈

## 책임과 범위

카카오 토큰 검증, JWT 발급·검증, Redis 기반 refresh token rotation(RTR), dev 전용 토큰 발급을 소유합니다. 기존 subject 조회·등록 이벤트 예제도 유지합니다.

예제 subject는 인증 증명이 아니며 접근 권한을 부여하지 않습니다. auth는 Redis 세션을 소유하며 사용자 원본·소셜 계정 연결은 user가 소유합니다.

## 로그인 API

| 메서드·경로 | 입력 | 결과 |
| --- | --- | --- |
| POST `/api/v1/auth/social/kakao` | `{"accessToken":"카카오 SDK 토큰"}` | 가입 또는 로그인 후 서비스 토큰 |
| POST `/api/v1/auth/refresh` | `{"refreshToken":"서비스 refresh token"}` | 새로운 access/refresh 쌍 |
| POST `/api/v1/auth/logout` | 같은 refresh 입력 | 해당 Redis 세션 폐기 |
| POST `/api/v1/auth/dev/tokens` | `X-Dev-Master-Key` 헤더 + `{"userId":1}` | 기존 사용자로 일반 토큰 발급 |

발급 응답의 `result`는 `tokenType: Bearer`, `accessToken`, `refreshToken`, UTC `accessTokenExpiresAt`, `refreshTokenExpiresAt`입니다. 응답은 `Cache-Control: no-store`를 사용합니다. 보호된 API에는 `Authorization: Bearer <accessToken>`을 전달합니다.

`SocialLoginResolver`는 `SocialLoginStrategy` Bean을 provider 이름으로 선택합니다. 카카오만 구현하며 Naver·Google은 확장 주석만 있습니다. 미지원 provider는 `AUTH-002`입니다.

카카오 전략은 [공식 토큰 정보 조회](https://developers.kakao.com/docs/ko/kakaologin/rest-api#access-token-info)로 앱 ID·사용자 ID·남은 만료시간을 검사하고 사용자 정보의 ID와도 대조합니다. 외부 응답은 경계에서 `SocialIdentity`로 변환하며 카카오 토큰 원문은 저장하지 않습니다. 닉네임이 없으면 기본 표시 이름을 사용합니다.

검증 후 `SocialSubjectAdapter`가 user의 `SocialUserRegistration`을 호출합니다. SQL 커밋 후 Redis 세션을 생성합니다. Redis 장애 시 `AUTH-007`(503)로 토큰 응답을 막고, 이미 등록된 사용자는 다음 로그인에서 재사용합니다.

## RTR 정책

- JWT의 HS256 서명·issuer·audience·용도·만료를 검사합니다. access와 refresh는 audience 및 `token_use`로 구분합니다.
- 로그인마다 별도 `sid`를 만들고 `<key-prefix>{sid}`에 현재 사용자 ID·refresh jti의 SHA-256 해시만 저장합니다.
- refresh의 서명·만료를 먼저 검증하고 Redis Lua로 현재 해시 비교와 교체를 원자적으로 수행합니다. TTL은 `KEEPTTL`로 보존하며 최초 세션 만료시각을 연장하지 않습니다.
- 검증된 이전 토큰 재사용은 같은 세션을 폐기합니다. 동시 동일 토큰 회전은 한 요청만 성공할 수 있고, 후속 요청의 재사용 감지로 새 refresh도 무효화됩니다. 클라이언트는 refresh 요청을 하나로 합쳐야 합니다.
- 다른 로그인·기기의 세션에는 영향이 없습니다. logout·재사용 폐기는 refresh에 적용되며 이미 발급된 access는 만료까지 유효합니다.
- Redis 데이터 유실 시 재로그인이 필요합니다. 운영 Redis의 지속성·접근 제어는 배포 환경에서 관리합니다.

## YAML과 실행 준비

`application.yml`의 `app.auth.token`에 서명키 참조, issuer·audience, access TTL(15분), refresh TTL(14일), 시계 오차(0초)를 둡니다. `app.auth.refresh-store.key-prefix`의 기본값은 `auth:refresh:`입니다. 환경별 prefix와 서명키를 분리합니다. `app.auth.kakao`는 앱 ID·API URI·연결/응답 제한시간을 관리합니다.

`JWT_SECRET`은 Base64로 인코딩한 최소 32바이트 무작위 키, `KAKAO_APP_ID`는 REST API 키가 아닌 숫자 앱 ID입니다. 실제 값은 환경변수로 주입하며 기본 비밀값은 없습니다. 필수 값 누락·오류는 시작 실패로 처리합니다. `.env.example`의 빈 값을 채운 뒤 실행합니다.

dev Controller·Service·비밀키 Adapter는 모두 `dev & !prod`에서만 활성화합니다. `application-dev.yml`의 `app.auth.dev.master-secret`은 최소 32바이트 `DEV_MASTER_SECRET`을 참조합니다. 비밀키 해시를 상수 시간 비교하고 검증 후 사용자 존재 여부를 조회합니다. prod가 함께 활성화되면 dev API는 차단됩니다.

dev의 `ddl-auto`는 `none`입니다. 기존 DB에는 아래 SQL을 별도 배포 절차로 적용해야 합니다. 앱은 이를 자동 실행하지 않습니다.

```sql
ALTER TABLE app_user ADD COLUMN social_provider varchar(32);
ALTER TABLE app_user ADD COLUMN social_subject varchar(128);
ALTER TABLE app_user ADD CONSTRAINT uk_app_user_social UNIQUE (social_provider, social_subject);
```

로컬·테스트 `create-drop`은 기존 승인·데이터 보존 규칙을 따릅니다. 인증 결정은 [ADR-006](../adr/006-social-login-and-refresh-rotation.md)에 있습니다.

## 조회 흐름

```text
GET /api/v1/auth/examples/subjects/{userId}
  → GetAuthSubjectQuery → GetAuthSubjectUseCase
  → GetAuthSubjectService → LoadAuthSubjectPort
  → UserSubjectAdapter → user.UserLookup
  → UserSummary의 id를 AuthSubject로 변환
  → AuthSubjectInfo → AuthSubjectResponse
```

- 양수 식별자에 해당하는 사용자가 있으면 `subject: "user:{id}"`를 반환합니다. 표시 이름은 auth에 전달하지 않습니다.
- `AuthSubject`는 양수 식별자와 `user:` subject 이름 규칙을 소유합니다. user의 Domain·DTO나 Spring/JPA에 의존하지 않습니다.
- 없는 사용자는 `AuthErrorCode.USER_NOT_FOUND` (`AUTH-001`, HTTP 404), 양수가 아닌 경로 값은 공통 입력 오류 (`COMMON-400`, HTTP 400)를 반환합니다.
- 공개 예제 경로는 `/api/v1/auth/examples/**`입니다. `/api/v1/auth/**` 전체를 공개하지 않습니다.

## 이벤트 흐름

```text
user.UserRegistered
  → UserRegisteredListener
  → RecordUserRegistrationCommand
  → RecordUserRegistrationUseCase
  → RecordUserRegistrationService
```

- 리스너가 공개 이벤트를 auth 소유 Command로 변환합니다. Application과 Domain은 user 타입을 참조하지 않습니다.
- 등록 커밋 후 별도 스레드·트랜잭션에서 `userId`, `occurredAt`만 로그로 기록합니다. 롤백 시 소비하지 않습니다.
- 발생 시각은 user의 발행 시각이며 커밋 시각이 아닙니다. 소비 실패가 이미 커밋한 등록을 되돌리지 않습니다.
- 로그 외 영속 모델·자동 재처리는 없습니다. 전달 보장은 [영속성·이벤트](../conventions/persistence-events.md)를 따릅니다.

## 패키지와 공개 계약

- 조회 계약·값: `application/port/in/query`, `query/dto`.
- 이벤트 후속 처리 계약·값: `application/port/in/command`, `command/dto`.
- 유스케이스와 resolver 구현은 `application/service`에 둡니다.
- 출력 Port는 `application/port/out`, user 연동 구현은 `adapter/out/user`에 둡니다.
- 현재 다른 모듈에 공개하는 타입은 없습니다. 허용 의존성은 `shared::error`, `shared::openapi`, `user`입니다.

소비 모듈 모델의 일반 기준은 [아키텍처](../conventions/architecture.md#소비-모듈의-모델과-외부-정보-변환), 이번 결정은 [ADR-002](../adr/002-application-contracts-and-auth-example.md)를 따릅니다.
