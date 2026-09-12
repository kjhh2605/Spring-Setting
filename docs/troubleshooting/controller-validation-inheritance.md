# Controller 인터페이스와 Bean Validation 충돌

## 증상과 조건

Controller가 Docs 인터페이스를 구현하고 `@Validated`로 실행 메서드 검증을 사용할 때, 요청이 `HV000151 ConstraintDeclarationException`으로 500을 반환할 수 있습니다. 인터페이스에 Path/Header 제약이 있고 구현 메서드가 Request Body에 `@Valid`를 추가한 경우 발생합니다.

## 원인과 해결

Bean Validation은 구현 메서드가 상위 메서드의 파라미터 제약 구성을 재정의하는 것을 금지합니다. `@Valid`도 cascade 검증 구성에 포함됩니다. 인터페이스에 `@Valid`를 추가하되 구현에도 남기면 다른 파라미터의 제약 구성과 함께 여전히 충돌할 수 있습니다.

Path/Header 제약과 Body의 `@Valid`를 Docs 인터페이스에 선언하고 구현 메서드에서는 제거합니다. Spring MVC의 HTTP 매핑과 `@RequestBody`는 Controller에 유지합니다. 요청 record의 필드 제약은 그대로 둡니다. Hibernate Validator의 오버라이드 허용 설정으로 검사를 우회하지 않습니다.

## 검증

`AuthLoginIntegrationTest`와 `DevTokenIntegrationTest`가 정상 요청·빈 토큰·잘못된 비밀키를 실제 HTTP 경계에서 검사합니다. 정상 요청이 200, 잘못된 입력이 400/401인지를 확인해야 하며 컴파일 성공만으로는 이 문제를 확인할 수 없습니다. 규칙은 [OpenAPI](../conventions/openapi-conventions.md)를 따릅니다.
