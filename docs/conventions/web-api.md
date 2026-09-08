# Web API와 DTO

HTTP 계약·Controller·Web Request/Response 변경에 적용합니다.

## HTTP 계약

- URI는 `/api/v{version}/{resource}`로 시작하고 리소스명은 복수형을 사용합니다.
- Request/Response는 Java `record`를 우선합니다. Request Body에는 `@Valid`, 경로·쿼리 제약에는 `@Validated`를 적용합니다.
- Adapter에서 Request를 Command/Query로 변환합니다. Web DTO를 Domain/Application 타입과 공유하거나 JPA Entity를 반환하지 않습니다.
- 정상 응답은 `success`, `code`, `message`, `result`로 감쌉니다. 도메인 오류는 모듈별 ErrorCode와 `BusinessException`으로 표현합니다.
- Controller는 `{module}.adapter.in.web.docs`의 `*ControllerDocs` 인터페이스를 구현합니다. 공개 HTTP 계약을 바꾸면 [OpenAPI 규칙](openapi-conventions.md)도 적용합니다. 성공 결과 타입과 실제 `BaseCode` enum 기반 오류를 선언하고 공통 커스터마이저가 래퍼를 반영합니다.

## Web DTO 구성

- Web DTO는 `{module}.adapter.in.web`에 둡니다. 타입이 적으면 평면으로, 독립 유스케이스가 늘면 `registration`, `profile`, `search` 등 함께 변경되는 기능별 하위 패키지로 묶습니다.
- 모듈 루트에 `dto`, `request`, `response` 기술 유형 패키지를 만들거나 `UserDtos`처럼 독립 DTO를 모음 타입에 담지 않습니다.
- 독립적으로 이름 붙일 수 있는 Request/Response는 파일 하나에 최상위 `record` 하나를 선언합니다.
- 중첩 `record`는 특정 상위 Request/Response에만 소유되고 그 밖에서 독립 의미가 없을 때만 사용합니다.
- 여러 API가 의미·노출 정책·변경 주기를 공유하는 부분 모델은 가장 가까운 공통 기능 패키지의 최상위 `record`로 추출합니다. 필드만 같고 의미·노출 정책이 다르면 별도 타입을 유지합니다.
- Web DTO는 모듈 간 공유하지 않습니다. 다른 모듈의 루트/named interface 계약을 받아 자신의 DTO로 변환합니다.

```text
{module}/adapter/in/web
├── ExampleController.java
├── docs
│   └── ExampleControllerDocs.java
├── registration
│   ├── RegisterExampleRequest.java
│   └── RegisterExampleResponse.java
└── profile
    ├── UpdateExampleProfileRequest.java
    └── ExampleProfileResponse.java
```

한 응답만 소유하는 부분 모델:

```java
public record UserDetailResponse(Long id, Profile profile) {

    public record Profile(String name, String profileImageUrl) {}
}
```

같은 간이 프로필을 여러 API가 공유한다면 독립 타입으로 추출합니다.

```java
public record UserProfileSummaryResponse(String name, String profileImageUrl) {}
```
