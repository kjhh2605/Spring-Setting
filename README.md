# Spring Modulith Backend Template

새 백엔드 프로젝트의 출발점으로 사용하는 단일 JAR 모듈러 모놀리스 템플릿입니다. `user`·`auth` 예제로 DDD 모듈 경계, 내부 Hexagonal Architecture, 공개 API와 비동기 이벤트 연동을 보여줍니다.

## 기술 스택

Java 21 · Spring Boot · Spring Modulith · Gradle Kotlin DSL. 정확한 버전은 [버전 카탈로그](gradle/libs.versions.toml)와 [빌드 설정](build.gradle.kts)에서 관리합니다.
Spring MVC/Validation/Security/Actuator/OpenAPI, Spring Data JPA/QueryDSL/PostgreSQL, JWT와 Redis를 사용합니다.
테스트는 JUnit Jupiter(Boot BOM 관리), Mockito, Modulith Test, Testcontainers입니다.

## 빠른 시작

JDK 21과 Docker/Compose가 필요합니다. 별도 Gradle 설치 없이 Wrapper를 사용합니다.

```bash
cp .env.example .env
# .env의 JWT_SECRET, KAKAO_APP_ID를 먼저 설정합니다.
docker compose --env-file .env up -d postgres redis
set -a && source .env && set +a
./gradlew bootRun
```

활성 프로필은 자동 선택하지 않습니다. 위 명령은 `.env`의 `SPRING_PROFILES_ACTIVE=local`을 적용합니다. 로컬/테스트는 `create-drop`이므로 보존할 데이터를 넣지 마세요.

기본 포트: API `8080`, Actuator `9090`.

- OpenAPI UI: `http://localhost:8080/docs`
- Health: `http://localhost:9090/actuator/health`
- Prometheus: `http://localhost:9090/actuator/prometheus`

예제 API와 Health는 공개입니다. 소셜 로그인에서 발급한 JWT로 보호된 API를 인증합니다. Prometheus·Info 수집기의 인증·접근 정책은 운영 환경에서 구성합니다. [로그인 API·필수 설정·스키마](docs/domain/auth.md)를 확인하세요.

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"displayName":"홍길동"}'

curl http://localhost:8080/api/v1/auth/examples/subjects/1
```

## 아키텍처

```text
src/main/java/com/example/   애플리케이션과 shared·user·auth 모듈
src/test/                   단위·모듈·API·아키텍처 테스트
gradle/                     버전·의존성·품질·테스트 설정과 Wrapper
config/checkstyle/          코드 검사 규칙
docker/                    실행 JAR용 컨테이너 이미지
.github/                   CI·이슈/PR 양식·라벨 정의·기여 안내
docs/
├── adr/                    템플릿 아키텍처 결정 이력
├── conventions/            주제별 개발 규칙
├── domain/                 모듈 책임과 공개 계약
└── onboarding/             새 프로젝트 적용과 실행 안내
```

- `shared`: 오류·OpenAPI 공개 계약과 내부 인프라.
- `user`: 사용자 등록·요약 조회·등록 이벤트.
- `auth`: 카카오 전략/resolver, JWT·Redis RTR·dev 발급, subject 조회 예제·등록 커밋 후 비동기 로그 처리. 영속 이벤트 저장소·자동 재처리는 없습니다.

소유권·공개 타입은 [도메인 지도](docs/domain/README.md), 의존성은 [아키텍처](docs/conventions/architecture.md)가 원본입니다. 운영 스키마는 자동 변경하지 않으며 배포 전에 마이그레이션 전략을 결정해야 합니다. 프로필·이벤트 경계는 [영속성·이벤트](docs/conventions/persistence-events.md)를 확인합니다.

## 검증

[전체 검증 명령](AGENTS.md)을 사용합니다. 전체 테스트에는 Docker가 필요하며 일부 건너뛰기를 통과로 보지 않습니다. 집중 검사·CI 보고서 정책은 [테스트 문서](docs/conventions/testing.md)에 있습니다.

## 문서 안내
- 실행 준비·문제 해결: [온보딩](docs/onboarding/README.md)
- 작업별 규칙: [컨벤션 목차](docs/conventions/backend-conventions.md)
- API 스키마: [OpenAPI](docs/conventions/openapi-conventions.md)
- AI 작업 방식: [AGENTS.md](AGENTS.md)
- 작업 유형·이슈·라벨·PR과 AI 작성·리뷰: [GitHub 작업 가이드](docs/conventions/github-workflow.md)
- 구조 선택의 이유와 제약: [기본 결정](docs/adr/001-template-architecture.md), [Application 분류·auth 예제](docs/adr/002-application-contracts-and-auth-example.md)
