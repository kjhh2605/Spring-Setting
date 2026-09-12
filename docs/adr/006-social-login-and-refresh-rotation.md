# ADR-006: 카카오 전략과 Redis refresh token rotation

- 상태: Accepted
- 기준일: 2026-09-12
- 대체 대상: [ADR-002](002-application-contracts-and-auth-example.md)의 실제 인증 미구현·공개 경로 제한 결정. 예제와 모듈 경계는 유지합니다.

## 배경

프론트 SDK의 카카오 access token을 검증하는 로그인과 Redis RTR이 필요합니다. 추가 제공자는 구현하지 않으며 만료시간·키는 YAML로 관리하고 개발 환경에서는 별도 비밀키로 기존 사용자 토큰을 발급해야 합니다.

## 결정

- auth 출력 Port `SocialLoginStrategy`를 카카오 Adapter가 구현하고 Application resolver가 provider 이름으로 선택합니다. Naver·Google은 확장 주석만 둡니다.
- 카카오 앱 ID·사용자 ID·만료를 검증합니다. user의 공개 `SocialUserRegistration`이 계정 연결을 소유하고, PostgreSQL 복합 유일 제약·충돌 처리로 동시 첫 로그인도 같은 사용자를 반환합니다. 이름·이메일로 계정을 합치지 않습니다.
- auth가 HS256 JWT를 발급하고 Spring Security Resource Server가 access를 검증합니다. refresh는 별도 audience·용도로 격리합니다.
- Redis에 로그인 세션별 현재 refresh 식별자 해시와 TTL을 저장합니다. Lua 원자 비교·교체로 회전하고 재사용 시 같은 세션을 폐기합니다. 최초 세션 만료시각은 연장하지 않습니다.
- logout·재사용 폐기는 refresh에 적용되고 기존 access는 만료까지 유효합니다. 매 API 요청의 Redis 조회를 통한 즉시 access 폐기는 도입하지 않습니다.
- 카카오 호출·SQL 커밋·Redis 저장을 하나의 트랜잭션으로 묶지 않습니다. Redis 실패 시 토큰 응답을 막으며 커밋된 사용자는 다음 로그인에 재사용합니다.
- 서명키·앱 ID·TTL·prefix·외부 URI·제한시간은 YAML에서 관리하고 실제 비밀값은 환경변수로 주입합니다.
- 개발용 발급 Controller·Service·Adapter는 `dev & !prod`에서만 제공합니다. 별도 비밀키 검증 후 존재하는 사용자로 일반 RTR 토큰을 발급합니다.

## 영향과 검증

JWT·Redis 의존성과 로컬 Redis Compose 서비스를 추가합니다. 기존 DB의 소셜 연결 컬럼·유일 제약은 별도 배포 절차로 적용하며 마이그레이션 도구는 추가하지 않습니다. 적용 SQL·설정·API는 [Auth 문서](../domain/auth.md)를 따릅니다.

전략·카카오 응답·JWT 만료와 용도·dev 프로필 검사, 실제 PostgreSQL 동시 등록·Redis 동시 회전·HTTP 로그인/재발급/로그아웃·OpenAPI·모듈/계층 검사를 수행합니다. 카카오 연동은 공식 응답 계약을 이용한 HTTP mock으로 검사하며 실제 계정 인증을 대신하지 않습니다.
