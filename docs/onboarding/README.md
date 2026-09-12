# 개발자 온보딩

## 준비물

JDK 21, Docker와 Docker Compose가 필요합니다. Gradle은 저장소 Wrapper를 사용합니다.

## 새 프로젝트로 시작

이 저장소를 새 저장소의 초기 코드로 사용한 뒤 아래 순서로 적용합니다.

1. 코드를 바꾸기 전에 [빠른 시작](../../README.md)으로 등록·조회 예제가 동작하는지 확인합니다.
2. 아래 식별자를 새 프로젝트 이름과 기본 패키지로 변경합니다. main·test의 패키지 경로, package 선언, import와 애플리케이션 클래스 참조를 함께 바꿉니다.
3. [도메인 지도](../domain/README.md)를 기준으로 `user`·`auth` 예제를 실제 유스케이스로 교체합니다. 모듈을 추가·제거하면 `allowedDependencies`, `ModularityTest`의 모듈 목록, 모듈별 테스트·지침·도메인 문서를 함께 갱신합니다.
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

패키지 경로를 옮기면 모듈·테스트 `AGENTS.md`의 상대 링크와 문서의 코드·테스트 경로도 갱신합니다. 운영 적용 전에는 인증·인가, 스키마 마이그레이션과 이벤트 전달 보장 요구를 [백엔드 ADR](../adr/001-backend-architecture.md)에 따라 결정합니다.

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
5. 바뀐 책임·정책의 도메인 문서와 해당 주제 ADR의 갱신을 변경 단위에 포함합니다. 독립적인 새 주제는 [ADR 관리 규칙](../adr/002-agentic-coding-rules.md#문서와-adr-관리)에 따릅니다. 단위마다 [집중 검사](../conventions/testing.md)·[계약 검토·커밋](../conventions/github-workflow.md#작업-중-커밋-체크포인트)을 마치고, 모든 단위와 알려진 수정 사항을 완료한 뒤 전체 검증을 수행합니다.

## 자주 쓰는 명령

```bash
./gradlew test --tests 'com.example.user.*'
./gradlew test --tests com.example.UserRegistrationEventIntegrationTest -PrequireAllTests=true
./gradlew spotlessApply
```

전체 명령은 [루트 검증 기준](../../AGENTS.md)을 따릅니다. IDE도 [같은 Spotless 설정](../conventions/code-style.md)을 사용합니다. CI 보고서는 `verification-reports` artifact에서 확인합니다.

## 에이전트 문서 탐색

에이전트 지침·스킬의 적용 범위를 점검할 때 읽습니다. 작업별 문서는 [컨벤션 목차](../conventions/backend-conventions.md), 모듈·테스트 지침의 적용 경로는 [루트 읽기 경로](../../AGENTS.md#읽기-경로)가 안내합니다.

### Codex의 공식 동작

아래 동작은 2026-09-12에 확인한 [AGENTS.md 공식 문서](https://learn.chatgpt.com/docs/agent-configuration/agents-md)와 [Skills 공식 문서](https://learn.chatgpt.com/docs/build-skills)를 기준으로 합니다. 실제 설치 버전·사용자 설정에 따라 확인합니다.

- Codex는 실행 시작 시 지침 체인을 구성합니다. 전역은 `CODEX_HOME`(기본 `~/.codex`)에서, 프로젝트는 저장소 루트부터 시작 작업 디렉터리까지 탐색합니다. 프로젝트를 찾지 못하면 현재 디렉터리만 확인합니다.
- 디렉터리마다 비어 있지 않은 `AGENTS.override.md` → `AGENTS.md` → 설정된 `project_doc_fallback_filenames` 중 하나를 선택합니다. 루트부터 합치며 가까운 디렉터리가 앞선 지침보다 우선합니다. 같은 디렉터리의 override와 기본 파일을 모두 합치지 않습니다.
- 프로젝트 지침의 합산 한도 `project_doc_max_bytes` 기본값은 32 KiB입니다. 파일별 한도가 아니며, 분할해도 같은 탐색 경로에 합쳐지는 크기는 줄지 않습니다. 연결된 Markdown 본문이나 시작 디렉터리 아래 지침 전체가 자동으로 로딩되는 것은 아닙니다.
- 로컬 저장소 스킬은 현재 디렉터리부터 저장소 루트까지의 `.agents/skills`에서 찾습니다. `SKILL.md`의 `name`·`description`으로 발견·선택하고, 선택한 스킬 본문과 필요한 참조만 읽습니다. 명시 호출과 description에 맞는 암시 호출이 가능하므로 수행할 반복 작업과 호출 조건을 구체적으로 씁니다.

### 이 저장소에서 확인할 것

- 루트에서 시작하면 `user`·`auth`·`shared`와 테스트 하위 지침은 자동 적용을 가정하지 않고 대상 작업에서 읽습니다. 테스트 경로에서 시작해도 소스 모듈 지침은 상위 경로가 아니므로 별도 확인합니다. 하위 지침은 각 모듈의 계약·예제 한계와 테스트 경계에 대한 추가 규칙입니다.
- `CLAUDE.md`·`GEMINI.md`·Copilot 지침은 공통 원본으로 이동하는 포인터입니다. Codex의 기본 탐색 파일로 가정하지 않습니다. 현재 저장소에는 `AGENTS.override.md`와 `SKILL.md`가 없으며, 협업 역할은 아래 프로젝트 에이전트 설정에서 관리합니다. 개인·설치 플러그인의 스킬 목록과 저장소 관리 파일은 구분합니다.
- 현재 문서는 작업별 참고 자료로 사용합니다. 스킬이 필요해지면 특정 반복 작업에 대한 입력·결과·호출 조건을 먼저 확인합니다. 참고 문서를 일괄 스킬로 바꾸거나 모든 작업에 스킬 읽기를 추가할 이유는 없습니다.

지침 변경 뒤에는 **새 세션**을 루트와 대상 하위 디렉터리에서 각각 시작하여, 추가 파일 읽기 전에 이미 로딩된 지침의 출처·적용 범위를 확인합니다. CLI가 있다면 `codex --version`, `codex exec --help`로 지원 옵션을 확인한 뒤 `codex exec --cd .`와 `codex exec --cd src/test/java/com/example`를 사용할 수 있습니다. 세션의 실제 입력·로그를 확인할 수 있으면 함께 대조합니다. 파일을 직접 읽어 경로를 따라가는 탐색 점검이나 모델의 추정만으로 실제 자동 로딩을 검증했다고 보고하지 않습니다.

### 운영 조언과 적용 판단

[Rethinking skills and prompts for GPT-6 Astra](https://developers.openai.com/blog/rethinking-skills-and-prompts-for-gpt-6-astra)는 호출 조건을 좁히고 작업과 무관한 문서 강제 읽기를 줄이라는 모델 운영 조언입니다. [Harness engineering](https://openai.com/index/harness-engineering/)의 짧은 진입 문서·저장소 지식 지도·기계적 경계 검증은 특정 OpenAI 팀의 사례이며 Codex의 필수 동작이 아닙니다. 두 글의 확인일은 2026-09-12입니다.

이 저장소에는 기존 인덱스와 테스트가 있으므로 조건부 읽기·원본 연결·검증 근거를 활용합니다. 이는 문서 탐색에 대한 적용 판단입니다. 사례의 문서 수·길이, 자동 병합 방식이나 모델에 따른 테스트 축소를 팀의 승인·CI·전체 검증 규칙으로 가져오지 않습니다.

## Codex 협업 설정

협업 설정을 추가·변경하거나 독립 검토를 위임할 때 읽습니다. 공식 [프로젝트 설정](https://learn.chatgpt.com/docs/config-file/config-basic), [Subagents](https://learn.chatgpt.com/docs/agent-configuration/subagents), [Rules](https://learn.chatgpt.com/docs/agent-configuration/rules), [Hooks](https://learn.chatgpt.com/docs/hooks)를 2026-09-12에 확인했습니다.

| 파일·기능 | 역할과 로딩 조건 | 이 저장소의 적용 |
| --- | --- | --- |
| `.codex/agents/*.toml` | 선택한 하위 에이전트의 역할 설정. 필수 값은 `name`, `description`, `developer_instructions` | 아래 검토 역할 2개. 모델·추론 강도·권한 설정은 부모 상속 |
| `.codex/config.toml` | 신뢰한 프로젝트의 설정 계층. CLI 옵션이 우선하고, 프로젝트 내부에서는 작업 디렉터리에 가까운 설정이 우선 | 역할 외에 공통으로 덮어쓸 설정이 없어 추가하지 않음. 역할 파일을 별도로 중복 등록하지 않음 |
| `.codex/rules/*.rules` | 신뢰한 프로젝트 계층에서 시작 시 읽는 실험적 명령 실행 정책. `prefix_rule`로 `allow`·`prompt`·`forbidden` 결정 | 아래 프로젝트 규칙 배치. 승인 경계는 AGENTS, 커밋·승인 요약은 GitHub 작업 가이드가 원본 |
| `.agents/skills/*/SKILL.md` | 특정 반복 작업의 선택형 절차. 메타데이터로 발견한 뒤 본문 로딩 | 현재 리뷰 절차는 기존 가이드와 역할 설정으로 충분하여 별도 스킬 없음 |
| `.codex/hooks.json` | 신뢰한 프로젝트의 수명주기 이벤트에 연결하는 자동 동작 | 반복 전체 검사나 자동 외부 작업을 추가할 근거가 없어 사용하지 않음 |

프로젝트 `.codex/` 계층은 신뢰 상태의 영향을 받습니다. 신뢰되지 않아 설정이 로딩되지 않는 경우 임의로 사용자 신뢰·승인 설정을 바꾸지 않고, 기존 지침으로 작업하고 미적용 사실을 알립니다. 현재 클라이언트의 역할 목록에서 아래 이름이 보이는지 확인합니다. 사용자·프로젝트·관리자 설정의 우선순위와 권한은 공식 가이드에 따릅니다.

### 역할 선택과 인계

| 역할 | 맡길 범위 | 원본 설정 |
| --- | --- | --- |
| `module_reviewer` | 모듈 책임·공개 계약·계층 의존성의 실제 변경과 소비자 영향 | [모듈 검토자](../../.codex/agents/module_reviewer.toml) |
| `verification_reviewer` | 테스트가 변경을 검출하는지, 린트·CI와 실행 보고에 빈틈이 있는지 | [검증 검토자](../../.codex/agents/verification_reviewer.toml) |

두 역할은 검토 업무를 위한 지시문이며, 파일 쓰기를 막는 별도 sandbox 설정은 아닙니다. 실제 권한은 부모 세션을 상속합니다. 기본 `worker`·`explorer` 역할과 함께 선택적으로 사용하고, 단순 수정에 병렬 실행을 강제하지 않습니다.

주 에이전트는 [기존 리뷰 기준](../conventions/github-workflow.md#pr-크기와-ai-리뷰)에 따라 같은 변경 기준과 요구사항, 담당 파일·diff 범위, 기대 결과를 전달합니다. 커밋 리뷰는 base/head SHA를, 미커밋 변경은 검토 시작 시 diff 범위를 명시합니다. 검토 중 변경이 생기면 영향받는 범위를 다시 확인합니다. 수정 위임이 필요한 작업은 파일 소유권을 나누고 공유 파일의 동시 수정을 피합니다. 하위 결과를 수집한 뒤 중복 지적·계약 연결·최종 검증은 주 에이전트가 확인합니다.

호출 예시:

```text
현재 미커밋 변경을 검토해줘. module_reviewer에게 모듈 계약과 소비자 영향을,
verification_reviewer에게 테스트·CI 검증 공백을 맡겨줘.
두 에이전트에는 각각 담당 diff 범위를 전달하고 파일은 수정하지 않게 해줘.
결과를 모아 파일 위치·영향·근거·미확인 사항을 정리해줘.
```

### 실행 정책을 검토할 때

승인 대상은 [루트 작업 원칙](../../AGENTS.md#작업-원칙), 요약할 정보는 [승인 절차](../conventions/github-workflow.md#승인-절차)를 따릅니다. 선택 배경은 [ADR-002의 승인과 외부 작업](../adr/002-agentic-coding-rules.md#승인과-외부-작업)에 있습니다.

[codex-commands.rules](../../.codex/rules/codex-commands.rules)는 **프로젝트 실행 규칙**입니다. `.codex/` 프로젝트 계층을 신뢰한 Codex가 시작 시 로딩하므로, 이동 전부터 실행 중인 세션에 자동 반영된다고 가정하지 않습니다. 규칙은 Git 이력 변경·푸시·브랜치/stash 삭제, Docker 자원 삭제, 앱 실행, PR 병합·닫기와 릴리스 생성의 일부 명령 형태에 `prompt`를 지정합니다. `git -C .`·`git branch -r -d`와 빠른 시작에서 사용하는 Compose `--env-file .env` 형태도 포함합니다. `allow` 규칙은 없으며, 자동 작업은 기존 세션 권한 안에서 수행한다는 뜻입니다.

삭제할 데이터가 없는 컨테이너 정리, 삭제 없는 프로필의 `bootRun`, 비공개 릴리스 초안 생성도 규칙에서는 보수적으로 확인합니다. 접두사로 실제 영향을 구별할 수 없기 때문이며, **행위별 승인 범위보다 추가 확인이 넓어질 수 있는 제약**입니다. 릴리스 명령의 태그·번호·본문 파일은 검사 예시이며 현재 저장소의 실제 릴리스·PR·산출물을 뜻하지 않습니다.

`.rules`는 명령의 인자 접두사를 비교하며 경로 읽기 제한이나 모든 도구의 접근 제어를 대신하지 않습니다. 다음 범위는 에이전트가 원본 승인 규칙에 따라 판단합니다.

| `.rules`만으로 판별하지 못하는 범위 | 적용할 처리 |
| --- | --- |
| 임의 경로의 `git -C … push`, `git commit -m … --amend`, 다른 옵션 순서·환경 파일 경로 | 고정 형태만 규칙에 포함됨. 불일치를 자동 허용으로 해석하지 않고 동일 행위의 요약·승인 의무 유지 |
| 스크립트·별칭·복잡한 shell wrapper, API·MCP를 통한 동일 행위 | 승인된 행위·대상 범위를 확인하며 다른 경로로 우회하지 않음 |
| 테스트 내부 SQL·자동 정리, `build`·`check`의 간접 테스트 실행 | 실제 task graph·테스트 구성을 확인하고 DB 삭제를 포함한 검사 전체를 사전 승인 |
| 다른 옵션 순서의 `bootRun`, IDE·JAR 실행 | 활성 프로필·대상 DB를 확인하고 `create-drop` 등 삭제를 포함한 실행·종료 범위를 사전 승인 |
| `git clean`·`restore`·`checkout`·`worktree remove`의 실제 작업 손실 | 미커밋·보관한 작업을 잃으면 사전 확인. 생성물 정리·미리보기·작업 파일을 보존하는 스테이징 조정과 구별하기 위해 포괄적 접두사 규칙을 두지 않음 |
| 기존 릴리스 초안의 공개 전환, CLI 외 PR 삭제 등 | `gh release edit … --draft=false` 등으로 공개하는 행위도 승인 대상. 요청한 일반 수정과 구별하며 존재하지 않는 삭제 명령은 가정하지 않음 |
| 새 커밋의 소유권·검증·기본 브랜치, 소스/생성물 삭제의 작업 관련성 | 커밋 규칙과 현재 diff·대상 파일로 판단 |
| 배포 | 저장소에 배포 대상·명령이 확정되어 있지 않으므로 명령 패턴을 추측해 등록하지 않음. 실행 준비 시 대상·변경을 요약하고 승인 |

DB 검사와 단위 검사를 접두사만으로 구분할 수 없어 `./gradlew` 전체 허용·일괄 승인 규칙은 넣지 않습니다. `not_match`는 해당 접두사 규칙의 비일치 사례이며 안전한 명령이라는 뜻이 아닙니다. 규칙을 모든 행위의 강제 차단 장치로 취급하지 않습니다.

저장소 루트에서 다음처럼 규칙을 검사합니다. 이 명령은 `git push`를 실행하거나 세션의 실제 로딩·승인 UI를 검증하지 않습니다.

```bash
codex execpolicy check --pretty --rules .codex/rules/codex-commands.rules -- git push origin HEAD
```

사용하는 클라이언트를 재시작한 뒤 프로젝트 신뢰 상태와 승인 모드에 따른 적용 여부를 확인합니다. 공식 Rules의 제어 대상은 샌드박스 밖 명령 실행이며, 파일 배치만으로 모든 실행 경로에 승인 UI가 보장되지는 않습니다. `prompt`가 사용자 승인 UI를 제공하는지 확인하며, 승인 요청을 표시할 수 없는 모드에서는 실행이 거부될 수 있습니다. 대화에서 승인받았다는 이유로 sandbox·승인 모드·프로젝트 신뢰 설정을 임의 변경하지 않습니다.

## 문제 해결

반복 조사로 확인된 상세 사례는 [트러블슈팅 색인](../troubleshooting/README.md)을 확인합니다.

- `All tests must run` 또는 컨테이너 테스트 건너뛰기: Docker daemon과 비활성화된 테스트를 확인합니다. 세부 [전체 실행 정책](../conventions/testing.md)을 우회하지 않습니다.
- DB 연결 실패: `.env`의 포트와 `docker compose ps`를 비교합니다.
- QueryDSL 타입 누락: `./gradlew compileJava` 후 IDE의 Gradle 모델을 다시 불러옵니다.
- 포맷 실패: `./gradlew spotlessApply` 후 `./gradlew spotlessCheck checkstyleMain checkstyleTest`. 빈 record·체인의 줄바꿈을 수동 복원하지 않습니다.
- 모듈 검증 실패: 예외를 추가하기 전에 다른 모듈 내부 패키지 import를 확인합니다.
