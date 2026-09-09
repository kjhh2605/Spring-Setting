# 개발자 온보딩

## 준비물

JDK 21, Docker와 Docker Compose가 필요합니다. Gradle은 저장소 Wrapper를 사용합니다.

## 새 프로젝트로 시작

이 저장소를 새 저장소의 초기 코드로 사용한 뒤 아래 순서로 적용합니다.

1. 코드를 바꾸기 전에 [빠른 시작](../../README.md)으로 등록·조회 예제가 동작하는지 확인합니다.
2. 아래 식별자를 새 프로젝트 이름과 기본 패키지로 변경합니다. main·test의 패키지 경로, package 선언, import와 애플리케이션 클래스 참조를 함께 바꿉니다.
3. [도메인 지도](../domain/README.md)를 기준으로 `user`·`activity` 예제를 실제 유스케이스로 교체합니다. 모듈을 추가·제거하면 `allowedDependencies`, `ModularityTest`의 모듈 목록, 모듈별 테스트·지침·도메인 문서를 함께 갱신합니다.
4. `.env.example`과 실행 환경의 DB·포트·CORS 값을 맞춥니다. 실제 환경값은 `.env` 또는 배포 환경에 두며 커밋하지 않습니다.
5. [전체 검증](../../AGENTS.md)을 실행한 뒤 프로젝트의 초기 기준점으로 삼습니다. 소스의 `port/out`·`adapter/out`과 테스트도 Git에 포함되어 있어야 합니다.
6. [GitHub 초기 설정](../conventions/github-workflow.md)에 따라 새 저장소에 라벨을 적용하고 기본 브랜치의 이슈·PR 양식을 확인합니다.

| 식별자 | 변경 위치 |
| --- | --- |
| Gradle 프로젝트 이름 | `settings.gradle.kts`의 `rootProject.name` |
| 그룹·버전·설명 | `build.gradle.kts`의 `group`, `version`, `description` |
| 애플리케이션 이름 | `src/main/resources/application.yml`의 `spring.application.name`, `logback-spring.xml`의 이름 기본값 |
| 기본 패키지·애플리케이션 클래스 | `src/main/java/com/example`, `src/test/java/com/example`, `SpringSettingsApplication` 참조 |
| Modulith 시스템 이름 | 애플리케이션 클래스의 `@Modulithic(systemName)` |
| 문자열로 지정한 패키지 | `ArchitectureTest`의 `importPackages`, 로깅 설정의 `com.example` |

패키지 경로를 옮기면 모듈·테스트 `AGENTS.md`의 상대 링크와 문서의 코드·테스트 경로도 갱신합니다. 운영 적용 전에는 인증·인가, 스키마 마이그레이션과 이벤트 전달 보장 요구를 [템플릿 ADR](../adr/001-template-architecture.md)에 따라 결정합니다.

## 로컬 실행

[README 빠른 시작](../../README.md)을 실행합니다. `.env.example`의 `SPRING_PROFILES_ACTIVE=local`을 불러와야 합니다. 로컬은 시작 시 스키마를 만들고 종료 시 제거하므로 컨테이너 볼륨에도 보존할 데이터를 넣지 마세요.

운영은 `SPRING_PROFILES_ACTIVE=prod`만 지정합니다. 필수 DB 환경변수·프로필 보호·운영 OpenAPI 제한은 [프로필 규칙](../conventions/persistence-events.md)을 따릅니다.

## 첫 확인

```bash
curl http://localhost:9090/actuator/health
```

이후 [README API 예제](../../README.md)로 등록·조회를 확인합니다. 정상 응답은 `success`, `code`, `message`, `result` 구조입니다. 등록 커밋 후 별도 스레드에 `userId`·발생 시각 로그가 남으며, API 응답 시점에 로그 처리가 끝났다고 가정하지 않습니다.

## 기능 추가 순서

1. [도메인 지도](../domain/README.md)에서 소유 모듈을 정하고 해당 모듈 지침을 읽습니다.
2. Domain/Application 테스트로 기대 행동의 실패를 확인하고 내부 Port·구현을 추가합니다.
3. 다른 모듈에 필요한 최소 계약만 루트 또는 shared의 책임별 named interface로 공개합니다.
4. Adapter를 연결하고 모듈·API 통합 테스트를 추가합니다. Web 변경은 [DTO](../conventions/web-api.md)와 [ControllerDocs](../conventions/openapi-conventions.md) 규칙을 따릅니다.
5. [집중 검사](../conventions/testing.md) 후 전체 검증을 수행하고, 바뀐 책임·정책의 도메인 문서·ADR을 갱신합니다.

## 자주 쓰는 명령

```bash
./gradlew test --tests 'com.example.user.*'
./gradlew test --tests com.example.UserRegistrationEventIntegrationTest -PrequireAllTests=true
./gradlew spotlessApply
```

전체 명령은 [루트 검증 기준](../../AGENTS.md)을 따릅니다. IDE도 [같은 Spotless 설정](../conventions/code-style.md)을 사용합니다. CI 보고서는 `verification-reports` artifact에서 확인합니다.

## 문제 해결

반복 조사로 확인된 상세 사례는 [트러블슈팅 색인](../troubleshooting/README.md)을 확인합니다.

- `All tests must run` 또는 컨테이너 테스트 건너뛰기: Docker daemon과 비활성화된 테스트를 확인합니다. 세부 [전체 실행 정책](../conventions/testing.md)을 우회하지 않습니다.
- DB 연결 실패: `.env`의 포트와 `docker compose ps`를 비교합니다.
- QueryDSL 타입 누락: `./gradlew compileJava` 후 IDE의 Gradle 모델을 다시 불러옵니다.
- 포맷 실패: `./gradlew spotlessApply` 후 `./gradlew spotlessCheck checkstyleMain checkstyleTest`. 빈 record·체인의 줄바꿈을 수동 복원하지 않습니다.
- 모듈 검증 실패: 예외를 추가하기 전에 다른 모듈 내부 패키지 import를 확인합니다.
