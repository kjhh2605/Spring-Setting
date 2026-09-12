# ADR-002: Application 계약 분류와 user·auth 예제

- 상태: Partially Superseded
- 변경일: 2026-09-12 — [ADR-006](006-social-login-and-refresh-rotation.md)
- 기준일: 2026-09-12
- 대체 대상: [ADR-001](001-template-architecture.md)의 「배포와 모듈 경계」 중 user·activity 예제 구성과 Application 내부 배치. 그 밖의 원칙은 아래 「유지하는 결정」에 따릅니다.

## 배경

입력 Port와 DTO, 서비스 구현의 평면 배치는 규모가 커질 때 계약과 구현을 찾기 어렵게 합니다. 기존 activity 예제는 user의 공개 DTO와 이벤트를 Application에서 직접 소비하므로 소비 모듈의 자체 모델·경계 변환을 보여주지 못했습니다.

## 결정

- 입력 Port는 상태 변경·후속 처리의 `application/port/in/command`와 조회의 `application/port/in/query`로 나눕니다.
- DTO는 사용하는 계약의 `command/dto`, `query/dto`에 둡니다. 등록 결과 `RegisteredUserInfo`도 command가 소유합니다. Info라는 접미사만으로 query에 배치하지 않습니다.
- 모듈 간 공개 계약은 루트에 유지합니다. `UserLookup`, `UserSummary`, `UserRegistered`를 내부 Port·DTO 패키지로 옮기지 않습니다.
- 서비스 구현은 `application/service`에 둡니다. 구현이 늘어 책임별 탐색이 필요할 때만 `service/command`, `service/query`로 세분화합니다. 없는 책임의 빈 패키지는 만들지 않습니다.
- user는 등록·순수 Domain·JPA Adapter·공개 요약 조회·이벤트 발행 예제로 구성합니다. 등록 기능의 표시 이름 불변식과 HTTP 계약은 유지합니다.
- activity 코드·테스트·API를 제거하고 auth 예제로 교체합니다. 기존 활동 API 및 `ACTIVITY-404`는 이 예제 제거에 따라 폐기합니다.
- auth는 공개 조회 결과를 `adapter/out/user`에서 자체 `AuthSubject`로, 공개 이벤트를 `adapter/in/event`에서 자체 Command로 변환합니다. Application과 Domain은 user 타입을 참조하지 않습니다.
- auth 조회는 `/api/v1/auth/examples/subjects/{userId}`에서 예제 subject를 반환하며 ~~실제 자격 증명 검증·토큰 발급·인증 상태 설정은 하지 않습니다. Security 공개 범위도 예제 경로로 제한합니다.~~ ([ADR-006으로 대체](006-social-login-and-refresh-rotation.md))

## 유지하는 결정

단일 JAR, Spring Modulith의 루트 공개 계약·shared named interface, 순수 Domain과 JPA Entity 분리, Web DTO와 Application DTO 분리, 커밋 후 비동기 이벤트 소비 및 전체 검증 정책을 유지합니다. Web DTO 배치 규칙은 이번 변경 대상이 아닙니다.

## 결과와 검증

계약과 구현의 탐색 위치가 일정해지고, 같은 사용자도 소비 모듈의 언어로 변환하는 예제를 제공합니다. 파일·변환 코드가 늘지만 ~~별도 DB, CQRS 인프라, 인증 제품 기능을 추가하지 않습니다.~~ ([ADR-006으로 대체](006-social-login-and-refresh-rotation.md))

`ModularityTest`로 shared·user·auth 구성과 모듈 경계, `ArchitectureTest`로 내부 의존성, API·OpenAPI 테스트로 공개 계약, `UserRegistrationEventIntegrationTest`로 실제 커밋·롤백·스레드·트랜잭션 경계를 검증합니다.

현재 규칙은 [아키텍처](../conventions/architecture.md), 모듈 책임은 [User](../domain/user.md)·[Auth](../domain/auth.md)를 따릅니다.
