# 포맷과 명명

Java 코드 작성·포맷·명명 변경에 적용합니다.

## 코드 포맷

- Java 레이아웃은 Spotless의 Palantir Java Format 결과를 따릅니다. 버전의 원본은 `gradle/libs.versions.toml`입니다.
- 공백 4칸, 줄 길이 120자를 기준으로 하며 이어지는 표현식·annotation·체인·빈 본문의 배치는 formatter에 맡깁니다. 이후 `formatAnnotations()`로 annotation 줄바꿈을 덮어쓰지 않습니다.
- import 순서는 static, `java`, `javax`, `jakarta`, `org`, `com`, 기타입니다. 미사용 import는 formatter가 제거하며 wildcard import는 금지합니다.
- UTF-8·LF를 사용합니다. Gradle은 공백 4칸, YAML은 2칸입니다. YAML 검사는 후행 공백·탭 변환·마지막 개행만 다루며 구조를 재정렬하지 않습니다.
- 코드 작성 후 `./gradlew spotlessApply`, 이어 `./gradlew spotlessCheck checkstyleMain checkstyleTest`를 실행합니다. CI는 자동 수정 없이 검사합니다.
- Checkstyle의 줄 길이·명명·코드 규칙을 지키며 검사 전체를 끄거나 formatter 결과를 수동 복원하지 않습니다. 제외 대상은 `build/generated/querydsl`뿐이며 직접 작성한 `Q*` 클래스도 검사합니다.
- IDE도 같은 Gradle Spotless 설정을 사용하고 저장 시 기본 formatter로 결과를 다시 바꾸지 않습니다.

선택 배경: [템플릿 아키텍처 결정](../adr/001-template-architecture.md).

## 이름 규칙

- `get...`: 없으면 예외, `find...`: `Optional<T>`, `list...`: null이 아닌 목록, `exists...`: 존재 여부.
- `from`: 하나의 원본 객체에서 생성합니다.
- 최상위 Web 입력은 `Request`, Application 입력은 `Command`/`Query`, 결과는 `Info`, Web 출력은 `Response` 접미사를 사용합니다.
- 중첩 부분 모델은 `Profile`처럼 문맥에 맞는 역할명을 사용합니다. 소유·재사용 기준은 DTO를 변경할 때 [Web DTO 규칙](web-api.md)을 확인합니다.
