# OpenAPI 문서화 규칙

## 문서 위치와 역할

- 각 Web Controller의 OpenAPI 문서 계약은 `docs` 하위 패키지에 `{ControllerName}Docs` 인터페이스로 둡니다.
- Controller는 Docs 인터페이스를 구현하고 HTTP 매핑, 검증, 유스케이스 호출만 담당합니다.
- `@Operation`, `@Tag`, Swagger `@ApiResponse`, `@Parameter`는 Docs 인터페이스에 작성합니다.
- 실제 Spring MVC의 `@RequestMapping`, `@RequestBody`, `@PathVariable`은 Controller에 유지합니다.
- Docs 인터페이스 메서드의 Path/Query 파라미터 제약은 Bean Validation 오버라이드 규칙에 따라 인터페이스에 한 번만 선언합니다. Request Body 필드 제약은 Request record에 둡니다.

```text
{module}/adapter/in/web
├── ExampleController.java
├── docs
│   └── ExampleControllerDocs.java
└── registration
    ├── RegisterExampleRequest.java
    └── RegisterExampleResponse.java
```

- Docs 인터페이스의 패키지는 `{module}.adapter.in.web.docs`로 통일합니다.
- Request·Response 타입은 `{module}.adapter.in.web` 또는 [기능별 하위 패키지](web-api.md)에 둡니다.
- Request·Response는 `docs` 하위 패키지에서 참조할 수 있게 `public`으로 선언합니다. 모듈 루트가 아닌 하위 패키지이므로 Spring Modulith의 모듈 공개 계약에는 포함되지 않습니다.

## Operation과 성공 응답

- 모든 공개 API에는 짧은 `summary`와 행위를 설명하는 `description`을 작성합니다.
- 성공 응답은 HTTP 상태, 설명, `application/json` 미디어 타입과 실제 Response 타입을 명시합니다.
- Docs 인터페이스에는 실제 결과 타입만 선언합니다. 공통 OpenAPI 커스터마이저가 런타임과 같은 `success`, `code`, `message`, `result` 구조로 감쌉니다. `result`는 값이 `null`이면 응답에서 생략되므로 선택 필드로 문서화합니다.
- FQN 스키마 이름을 사용하므로 모듈별로 같은 Request/Response 이름을 사용할 수 있습니다.

```java
@Operation(summary = "예제 조회", description = "식별자로 예제를 조회합니다.")
@ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ExampleResponse.class)
        )
)
ExampleResponse getExample(Long exampleId);
```

## 오류 응답

- 문서화할 ErrorCode enum은 `BaseCode`를 구현합니다.
- `@ApiErrorCodes`의 `enumClass`와 `includes`에 실제 enum 상수 이름을 지정합니다.
- 같은 Operation에 여러 `@ApiErrorCodes`를 선언할 수 있습니다.
- 공통 입력 오류는 `CommonErrorCode`, 모듈 오류는 해당 모듈의 ErrorCode를 사용합니다.
- `BaseCode`와 `CommonErrorCode`는 `shared::error`, `ApiErrorCodes`는 `shared::openapi` 공개 계약에서 사용합니다.
- 존재하지 않는 enum 상수 이름은 OpenAPI 생성 실패로 처리하여 잘못된 문서를 조기에 발견합니다.

```java
@ApiErrorCodes(enumClass = CommonErrorCode.class, includes = "BAD_REQUEST")
@ApiErrorCodes(enumClass = ExampleErrorCode.class, includes = "NOT_FOUND")
```

커스터마이저는 HTTP 상태별 공통 실패 구조와 ErrorCode의 코드·메시지 예시를 생성합니다. ControllerDocs에서 JSON 예시를 중복 작성하지 않습니다.

## 파라미터와 보안

- Path와 Query 파라미터에는 의미, 예시, 필수 여부를 작성합니다.
- Request Body의 필드 설명과 예시는 Request record의 `@Schema`로 표현합니다.
- Bearer 인증 스키마는 전역으로 등록하지만 모든 API에 강제하지 않습니다.
- 인증이 필요한 Operation에만 `@SecurityRequirement(name = "Bearer Authentication")`을 선언합니다.
- 인증 처리용 내부 파라미터는 `@Parameter(hidden = true)`로 숨깁니다.

## 검증

- `/docs-json` 계약 테스트에서 경로, summary, 성공 응답 래퍼와 대표 오류 코드를 확인합니다.
- ControllerDocs·스키마·커스터마이저 변경은 Swagger UI뿐 아니라 생성된 JSON 계약도 검증합니다. Markdown 설명만의 변경에는 루트의 문서 검증 기준을 적용합니다.
- ControllerDocs는 자기 모듈의 ErrorCode와 `shared::error`의 공개 오류 계약을 사용합니다. 다른 모듈의 내부 오류 타입에 접근하지 않는지 `ModularityTest`로 확인합니다.
