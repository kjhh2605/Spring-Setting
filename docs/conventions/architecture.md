# 아키텍처와 수정 위치

Domain/Application·패키지·모듈 계약 변경에 적용합니다. 현재 모듈의 책임·공개 타입·허용 의존성은 [도메인 지도](../domain/README.md)를 확인합니다.

## 패키지와 모듈 경계

`com.example` 아래 Application Module은 `shared`, `user`, `activity`입니다. 최상위를 비즈니스 책임으로 나누며 `controller`, `service`, `repository`, `entity`, `dto` 기술 계층 패키지를 만들지 않습니다.

```text
com.example
├── shared
│   ├── error          # named interface
│   ├── openapi        # named interface
│   └── internal
└── {business-module}
    ├── package-info.java
    ├── PublicApi.java
    ├── domain
    ├── application/port/{in,out}
    └── adapter/{in,out}
```

- 비즈니스 모듈의 공개 계약은 모듈 루트, `shared`의 공개 계약은 명시적 `@NamedInterface`에 둡니다. 다른 모듈의 `domain`, `application`, `adapter` 직접 접근이나 모듈 간 JPA Entity 공유는 금지합니다.
- `package-info.java`의 `allowedDependencies`는 실제 의존성만 선언합니다. `shared::error`처럼 한정하고 `shared::*`로 일괄 허용하지 않습니다.
- 내부 타입을 공개해 검증을 우회하지 않습니다. 필요한 최소 계약을 설계합니다.
- 즉시 응답은 공개 인터페이스, 완료 사실 전파는 공개 이벤트를 사용합니다. `activity`는 `user`의 공개 조회 계약과 이벤트만 사용합니다.

## Shared 공개 계약

- 오류와 OpenAPI 계약은 각각 `shared::error`, `shared::openapi`로 공개합니다. 공개 타입 목록·소비 모듈은 [도메인 지도](../domain/README.md)가 원본입니다.
- 설정·공통 응답·예외 처리·보안 구현은 `shared.internal`에 두고 공개하지 않습니다.
- 실제 여러 모듈이 재사용하는 작고 안정적인 계약만 `shared`에 둡니다. 한 모듈 전용이거나 사용처 없는 타입을 이동하지 않습니다.
- 페이지 요청·응답은 소유 모듈의 Web Adapter에 먼저 둡니다. 둘 이상 모듈에서 의미·노출 정책·변경 주기가 같아질 때만 기술 중립적인 공통 계약을 설계합니다.

## 모듈 내부 의존성

- 소스 의존성은 Adapter에서 Application의 Port와 Domain으로 향합니다. Domain은 Application/Adapter 및 Spring·JPA·Web 기술에 의존하지 않습니다.
- 입력 Adapter는 입력 Port를 호출하고 Application Service 구현·출력 Port·Persistence를 직접 호출하지 않습니다.
- Application Service는 Domain과 Port에 의존하고 Adapter·영속 기술에 직접 의존하지 않습니다. 출력 Adapter는 출력 Port를 구현합니다.
- `ModularityTest`는 모듈 간 계약, `ArchitectureTest`는 내부 의존성과 JPA Entity 위치를 검사합니다. 구체적 검사는 [테스트 규칙](testing.md)에 있습니다.

## Application과 Domain

- Request는 Adapter에서 Command/Query로 변환합니다. Controller는 Repository/Persistence Adapter를 직접 호출하거나 JPA Entity를 반환하지 않습니다.
- 식별자·값의 불변 조건은 Domain이 보장합니다. Domain에 Spring/JPA/Web annotation을 넣지 않습니다.
- 트랜잭션·시간·Entity 규칙은 해당 코드를 변경할 때 [영속성·이벤트](persistence-events.md)를 적용합니다.

## 오류 코드와 불변식

- 모듈 전용 오류 코드는 `{module}.application.error`에서 관리합니다. 모듈 내부 타입으로 유지하며 루트 공개 계약이나 `CommonErrorCode`에 추가하지 않습니다.
- `CommonErrorCode`에는 여러 모듈에 공통인 입력·인증·시스템 오류만 둡니다.
- Application은 `BusinessException(BaseCode)`로 실패를 전달하고, 기존 전역 처리기가 HTTP 응답을 만듭니다. 응답과 OpenAPI가 같은 코드를 사용하도록 Application 오류의 `HttpStatus` 결합을 허용합니다.
- Domain의 불변식 오류는 `BaseCode`, `BusinessException`, `HttpStatus`에 의존하지 않습니다. 필요한 API 오류 변환은 Application 또는 Web 경계에서 명시적으로 처리합니다.
- 신규 오류 코드 문자열은 HTTP 상태와 독립적인 `{MODULE}-{일련번호}` 형식(예: `ACTIVITY-001`)을 사용합니다. 코드는 중복·재사용하지 않으며 HTTP 상태가 바뀌어도 식별자는 유지합니다.
- 기존 `ACTIVITY-404`, `COMMON-400` 등의 응답 코드는 호환성을 위해 유지합니다. 신규 코드 명명 규칙을 기존 코드 변경 사유로 사용하지 않습니다.

## 어디를 수정할지

아래 경로는 `src/main/java/com/example/{module}` 기준입니다.

| 책임 | 위치 |
| --- | --- |
| HTTP 요청·응답 | `adapter/in/web` |
| 이벤트 소비 | `adapter/in/event` |
| 유스케이스 계약 | `application/port/in` |
| 외부 기술 계약 | `application/port/out` |
| 트랜잭션 흐름 | `application` |
| 모듈 전용 오류 코드 | `application/error` |
| 비즈니스 불변식 | `domain` |
| JPA·외부 연동 | `adapter/out` |
| 모듈 간 계약 | 모듈 루트, shared의 책임별 named interface |
| 전역 설정·응답·보안 | `src/main/java/com/example/shared/internal` (저장소 루트 기준) |

선택 배경과 제약: [템플릿 아키텍처 결정](../adr/001-template-architecture.md).
