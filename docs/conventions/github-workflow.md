# 이슈·라벨·PR 작성과 리뷰 가이드

사람과 AI가 같은 양식으로 문제·변경·검증을 전달하기 위한 규칙입니다. 작업 권한과 검증 범위는 [AGENTS.md](../../AGENTS.md)가 원본입니다. 이 문서는 GitHub에 게시할 내용의 작성 방식, AI 리뷰 절차와 새 저장소 설정을 다룹니다.

## 구성과 선택 이유

GitHub는 작고 목적이 분명한 PR, 변경 배경과 결과, 작성자의 사전 diff 검토를 권장합니다. 이를 기준으로 이슈는 요구와 완료 조건, PR은 실제 변경과 검증 근거를 담습니다. [GitHub PR 권장사항](https://docs.github.com/en/pull-requests/concepts/helping-others-review-your-changes)

작업 유형은 11개로 구분하고, 이슈는 필요한 정보에 따라 6종의 YAML Form을 사용합니다. PR은 자동으로 채워지는 공통 Markdown 양식 하나를 사용하고 유형별 설명을 달리합니다. 여러 PR 양식은 `template` 쿼리로 선택해야 하므로 이 템플릿에서는 선택 절차와 공통 항목 중복을 줄입니다. [이슈 양식 문법](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/syntax-for-issue-forms), [PR 양식 설정](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/creating-a-pull-request-template-for-your-repository)

분류 어휘는 Conventional Commits를 참고합니다. 규격이 의미를 정한 `feat`·`fix` 외의 유형은 프로젝트에서 선택할 수 있으며, `investigation`은 조사·논의를 위한 자체 유형입니다. 작업 유형의 수와 입력 양식의 수를 일치시킬 필요는 없습니다. [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)

입력 필드 원본은 [이슈 양식](../../.github/ISSUE_TEMPLATE/)과 [PR 양식](../../.github/pull_request_template.md), 라벨 이름·색상·설명 원본은 [labels.json](../../.github/labels.json)입니다. 양식을 바꿀 때는 이 문서의 선택 기준도 함께 맞춥니다. 조직별 Issue Type·Project·담당자는 미리 지정하지 않아 개인·조직 저장소에서 재사용할 수 있습니다.

## 작업 유형과 이슈 양식

유형 이름에 `type:`을 붙인 라벨을 사용합니다. 아래 표는 분류 경계와 양식 선택 기준이며, 개별 라벨의 이름·색상·짧은 설명은 [정의 파일](../../.github/labels.json)이 원본입니다.

| 유형 | 선택 기준 | 이슈 양식 |
| --- | --- | --- |
| `feat` | 새로운 기능·API·외부 동작을 제공하는 작업 | [기능](../../.github/ISSUE_TEMPLATE/02-feature.yml) |
| `fix` | 기대 동작·합의된 계약과 다른 결과를 바로잡는 작업 | [버그](../../.github/ISSUE_TEMPLATE/01-bug.yml) |
| `docs` | 문서·설명·가이드만 바꾸는 작업. 실제 API 동작·계약도 변경하면 그 목적의 유형 사용 | [문서](../../.github/ISSUE_TEMPLATE/04-docs.yml) |
| `refactor` | 외부 동작을 보존하며 책임·의존성·코드 구조를 개선하는 작업 | [구조/성능 개선](../../.github/ISSUE_TEMPLATE/03-improvement.yml) |
| `perf` | 응답 시간·처리량·자원 사용량의 개선이 주목적인 작업 | [구조/성능 개선](../../.github/ISSUE_TEMPLATE/03-improvement.yml) |
| `test` | 테스트 자체나 테스트 환경의 개선이 주목적인 작업 | [일반 작업](../../.github/ISSUE_TEMPLATE/05-task.yml) |
| `build` | Gradle·패키징·의존성 변경이 주목적인 작업 | [일반 작업](../../.github/ISSUE_TEMPLATE/05-task.yml) |
| `ci` | CI 워크플로·자동 검사 실행 절차를 바꾸는 작업 | [일반 작업](../../.github/ISSUE_TEMPLATE/05-task.yml) |
| `chore` | 다른 유형에 속하지 않는 저장소 관리·보조 설정·유지보수 | [일반 작업](../../.github/ISSUE_TEMPLATE/05-task.yml) |
| `revert` | 특정 PR·커밋의 변경을 되돌리는 작업 | [일반 작업](../../.github/ISSUE_TEMPLATE/05-task.yml) |
| `investigation` | 불확실성을 해소하는 조사·질문·대안 비교와 결론 정리 | [조사](../../.github/ISSUE_TEMPLATE/06-investigation.yml) |

대표 유형은 작업 목적을 기준으로 하나만 선택합니다. 버그 수정에 테스트·문서가 포함되어도 `fix`이고, 테스트만 보강하면 `test`입니다. `chore`로 분류하기 전에 더 구체적인 유형이 맞는지 확인합니다. 포맷만의 변경은 `chore`로 분류하고, 다른 작업에 수반된 포맷은 그 작업 유형을 따릅니다. 독립된 목적이 섞이면 이슈·PR을 나눕니다.

구조/성능 개선·일반 작업 Form은 `작업 유형`을 필수로 선택합니다. GitHub Form은 선택값을 라벨로 자동 변환하지 않으므로 두 양식은 처음에 `status:needs-triage`만 지정합니다. 라벨을 변경할 수 있는 작성자 또는 분류 담당자가 선택값을 확인해 정확한 `type:*` 하나를 붙입니다. 다른 네 양식은 고정 유형을 기본 지정합니다. CLI/API 작성에서는 선택한 유형 라벨도 직접 전달합니다.

## 이슈 작성

- 제목은 대상과 문제 또는 원하는 결과를 한 문장으로 적습니다. 유형은 라벨로 구분하므로 제목 접두사를 강제하지 않습니다. 예: `빈 사용자 이름으로 등록할 때 500 응답이 발생한다`는 작성 형식 예시이며 현재 구현의 결함을 뜻하지 않습니다.
- 새 이슈 작성 전에 관련 이슈를 확인하고, 같은 문제면 기존 이슈에 근거를 보탭니다. 접근할 수 없었다면 중복 확인을 완료했다고 적지 않습니다.
- 재현 가능한 관찰 사실과 원인 가설을 구분합니다. 완료 조건은 결과로 확인할 수 있게 쓰고, 조사에는 답할 질문과 종료 조건을 둡니다.
- `perf`는 성능 기준선·측정 조건을, `revert`는 대상 PR·커밋과 복구 조건을 작성합니다. 공용 양식의 해당 필드는 다른 유형도 사용하므로 웹에서 조건부 필수 입력을 강제하지 않습니다. 분류할 때 누락을 확인하고, 아직 측정하지 않았다면 수치 대신 미측정 사실과 계획을 적습니다.
- 필수 항목은 채우고 관련 없는 선택 항목은 비웁니다. 알 수 없는 사실은 `미확인`으로 표시합니다. 로그·요청·화면은 필요한 부분만 첨부하고 실제 비밀값·개인정보를 제거합니다.

조사 결론은 이슈에 남기고, 확정된 구조·정책만 기존 컨벤션과 [단일 ADR](../adr/001-template-architecture.md)에 반영합니다. 코드·상시 문서 변경이 없으면 PR 없이 이슈에서 마무리합니다.

## 라벨 운영

라벨은 이슈·PR에 공통으로 사용합니다. 다음은 조합 규칙이며 개별 의미는 [정의 파일](../../.github/labels.json)에서 관리합니다.

| 분류 | 적용 기준 |
| --- | --- |
| `type:*` | 분류가 끝난 이슈·PR에 대표 목적 1개. 고정 유형 Form은 기본 지정하고 공용 Form·PR·CLI/API는 선택한 유형을 직접 지정합니다. |
| `area:*` | 실제 영향 영역만 필요한 만큼. 문서만 수정하면 `area:docs`, CI 설정이면 `area:build` 등을 선택합니다. |
| `priority:*` | 담당자가 영향·긴급성을 판단한 뒤 최대 1개. 미분류와 낮은 우선순위를 구분하며 PR에 기계적으로 복사하지 않습니다. |
| `status:needs-triage` | 신규 이슈에만 기본 지정. 문제·중복·범위와 유형별 입력을 확인하고 대표 유형 라벨을 지정한 사람이 제거합니다. |
| `status:blocked` | 막는 이슈·결정과 해제 조건을 본문 또는 댓글에 연결하고 지정합니다. 해소되면 제거합니다. |
| `compatibility:breaking` | 기존 소비자의 대응이 필요할 때 지정하고 영향·전환 방법을 본문에 설명합니다. |

진행 중·리뷰 중·완료는 Issue/PR 상태와 사용하는 Project에서 관리합니다. 같은 의미의 상태 라벨을 추가하지 않습니다. 기존 GitHub 기본 라벨을 삭제할 필요는 없지만 이 규칙과 같은 의미의 라벨을 중복 적용하지 않습니다.

## PR 작성

제목은 `type(scope): 최종 변경 결과` 형식으로 작성하며 `scope`는 생략할 수 있습니다. 유형은 위 11개 중 하나, 범위는 실제 모듈·영역 이름을 사용합니다. 예: `docs(onboarding): 초기 실행 절차를 정리한다`. 호환성을 깨는 변경은 `feat(api)!: ...`처럼 `!`를 붙이고 `compatibility:breaking` 라벨과 전환 설명을 함께 작성합니다.

PR의 대표 라벨은 제목 유형과 일치시킵니다. 예를 들어 `fix(user): ...`에는 `type:fix`를 지정합니다. 유형은 최종 diff의 목적을 기준으로 정하므로 조사 이슈에서 시작한 기능 구현 PR은 `feat`가 될 수 있습니다. 이슈의 유형을 기계적으로 복사하지 않습니다.

본문은 [PR 양식](../../.github/pull_request_template.md)의 순서대로 작성하고 안내 주석은 제거합니다. 간단한 변경은 필수 항목마다 한 문장으로 충분합니다. 관련 이슈가 없으면 `없음`으로 적으며 형식만 맞추기 위해 이슈를 만들지 않습니다.

| 유형 | 주요 변경과 검증에 포함할 정보 |
| --- | --- |
| `feat` | 핵심 흐름, 완료 조건의 충족 여부, 정상·실패 경로 검증 |
| `fix` | 발생 조건·원인·수정 후 결과, 재현 테스트의 실패→성공 근거 |
| `docs` | 문서의 원본 근거, 구현과의 일치, 링크·경로·예제 확인 |
| `refactor` | 책임·의존성의 변경 이유와 기존 동작·계약 보존 근거 |
| `perf` | 같은 데이터·환경·명령으로 측정한 전후 수치, 측정 한계와 회귀 검사. 미측정이면 개선을 입증했다고 쓰지 않음 |
| `test` | 새로 검증하는 행동·실패 조건과 실제 테스트 결과 |
| `build` | 의존성·빌드·패키징 영향과 변경 후 실행 결과 |
| `ci` | 실행 조건·권한·검사 절차 변화와 검증 결과. 실제 CI 미실행은 로컬 검사와 구분 |
| `chore` | 구체적인 관리 대상·목적과 그 대상에 맞는 확인 결과 |
| `revert` | 대상 PR·커밋, 되돌리는 이유, 복구 상태와 데이터·설정 복구 제약, 복구 검증 |
| `investigation` | 사용한 자료·실험, 근거 있는 결론과 미해결 질문 또는 후속 작업 |

검증에는 실제 명령과 결과를 기록합니다. 성공·실패·건너뛰기·미실행을 구분하고 집중 테스트를 전체 테스트처럼 표현하지 않습니다. 필수 명령과 문서만 변경했을 때의 기준은 [검증과 완료](../../AGENTS.md)를 참조합니다. 큰 diff에는 리뷰 순서나 핵심 파일을 알려주고, 영향이 없으면 선택 항목인 `영향과 리뷰 포인트`를 제거합니다. 호환성 파괴·데이터 변경에는 적용 순서와 복구 방법 또는 복구 제약을 적습니다.

이슈 완료 조건을 모두 해결하는 PR에만 `Closes #123`을 사용합니다. 부분 작업·참고는 `Refs #123`으로 연결합니다. 번호는 형식 예시이므로 실제 존재와 관계를 확인해 교체합니다. 자동 종료는 기본 브랜치를 대상으로 한 PR이 병합될 때 적용되므로 다른 대상 브랜치에서는 종료를 보장하지 않습니다. [이슈와 PR 연결](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/linking-a-pull-request-to-an-issue)

## AI 작성 절차

1. 사용자 요청이 초안 작성인지 실제 게시까지 포함하는지 [작업 원칙](../../AGENTS.md)에 따라 구분합니다. 이미 받은 권한을 다시 묻지 않으며, 양식 추가 요청을 실제 이슈·PR 게시 요청으로 확대하지 않습니다.
2. 요청한 범위의 코드·문서·이슈와 현재 Git 상태를 확인합니다. PR은 기준 브랜치와 제출할 최종 diff를 확인하고, 작업 트리에 함께 있는 변경을 PR에 포함된 변경으로 오인하지 않습니다.
3. 작업 목적에서 유형을 정한 뒤 대응하는 양식을 읽습니다. 이슈 Form을 CLI/API로 대신 작성하면 각 입력 필드의 `label`을 Markdown 소제목으로 쓰고 필수 내용을 채웁니다. 공용 양식의 선택 유형과 `perf`·`revert`의 추가 입력도 포함합니다. `markdown` 안내와 비어 있는 선택 필드는 제외하고, 양식 기본 라벨과 정확한 유형 라벨 하나를 명시적으로 지정합니다. PR은 제목 유형·라벨·최종 diff의 목적을 일치시킵니다.
4. 사실·가설·미확인을 구분하고 실제 검증 기록만 인용합니다. 실행 계획을 통과 결과로 쓰거나 이전 테스트 결과를 이번 검증으로 쓰지 않습니다. 이슈 번호·링크·담당자·일정·체크 항목을 추정으로 채우지 않습니다.
5. 이슈 요구사항이나 대화 전체를 복사하지 말고 결과를 먼저 설명합니다. 범위가 바뀌었으면 제목·본문을 최종 변경 기준으로 다시 작성합니다. 작업 일지·임시 계획은 저장소 문서로 누적하지 않습니다.
6. 초안은 사용자의 언어로 제목·본문·라벨을 함께 제시합니다. 게시가 허용된 경우 대상 저장소·브랜치와 연결할 이슈를 확인해 게시하고 URL을 보고합니다. 구현이나 필수 검증이 미완료라면 Draft PR로 게시하고 남은 내용을 본문에 표시합니다.

GitHub 웹 Form과 CLI/API는 별도 경로입니다. 웹 필수 입력 설정이나 기본 라벨이 CLI/API에도 자동 적용된다고 가정하지 않습니다. 여러 줄 본문은 임시 파일에 실제 줄바꿈으로 작성해 `gh issue create --body-file` 또는 `gh pr create --body-file`로 전달합니다. GitHub에 게시할 파일 링크는 대상 저장소·브랜치 또는 커밋의 실제 URL을 사용하고 로컬 절대 경로를 넣지 않습니다. [이슈 CLI](https://cli.github.com/manual/gh_issue_create), [PR CLI](https://cli.github.com/manual/gh_pr_create)

## PR 크기와 AI 리뷰

이 프로젝트는 리뷰 한 묶음의 목표를 **변경된 텍스트 약 500줄**로 둡니다. 이는 추가 줄과 삭제 줄의 합계이며 diff의 문맥 줄은 세지 않습니다. 코드·테스트·설정·문서 모두 포함합니다. GitHub의 필수 제한이나 통과 기준이 아니라 검토 범위를 나누는 프로젝트 기준입니다.

작성 단계에서 목적이 독립적인 변경은 별도 PR로 나눕니다. 각 PR은 자체로 설명·검증할 수 있어야 하며, 후속 PR이 필요하면 의존 관계를 연결합니다. 변경량이 목표를 크게 넘을 것으로 예상되면 구현 전에 분리할 수 있는 목적과 경계를 정합니다. 단일 목적의 변경을 코드·필수 테스트·계약이 서로 맞지 않는 상태로 잘라 제출하지 않습니다. 작은 PR을 권장하는 근거는 [GitHub PR 권장사항](https://docs.github.com/en/pull-requests/concepts/helping-others-review-your-changes)을 따릅니다.

AI 리뷰는 아래 순서로 진행합니다. 리뷰 요청만 받은 경우 코드 수정이나 리뷰 댓글 게시로 범위를 확대하지 않습니다.

1. **전체 범위 파악**: PR 목적·완료 조건과 base/head 커밋 SHA를 확인합니다. `git diff --stat <base-sha>...<head-sha>`와 `git diff --numstat <base-sha>...<head-sha>`로 파일 목록과 변경량을 확인하고, 모듈·공개 계약·호출 관계를 먼저 파악합니다. 이후 리뷰는 같은 커밋을 기준으로 진행합니다.
2. **리뷰 묶음 구성**: 관련 파일·함수·diff hunk를 묶어 추가·삭제 합계 약 500줄씩 나눕니다. 고정 줄 수로 텍스트를 잘라 함수나 계약의 중간을 끊지 않습니다. 같은 변경의 구현·호출부·테스트를 가급적 함께 보고, 하나의 의미 단위가 목표를 넘으면 이유를 기록하고 함께 검토합니다. 이해에 필요한 주변 코드를 읽는 양은 이 목표로 제한하지 않습니다.
3. **묶음별 검토**: 요구사항·정확성·실패 경로·테스트 누락을 확인합니다. 묶음마다 대상 파일과 hunk 범위, 검토 완료 여부, 다른 묶음에서 확인할 사항을 기록합니다. 발견 사항은 파일·라인, 발생 조건, 영향과 근거를 갖춰 적고 가설을 확정 결함처럼 보고하지 않습니다.
4. **전체 연결 확인**: 모든 묶음을 본 뒤 호출부·공개 계약·구현·설정·테스트가 서로 일치하는지 확인합니다. 모듈 경계와 변경 전파는 [아키텍처 규칙](architecture.md)을 따릅니다. 묶음 사이의 미확인 사항을 해소하고 같은 원인의 지적을 합칩니다.
5. **완료 보고**: 중요한 결함부터 제시하고 검토한 커밋·묶음 수·검증 결과·남은 미확인 범위를 알립니다. 일부 묶음만 검토하고 전체 리뷰 완료로 보고하지 않습니다. head가 바뀌면 새 diff와 영향받는 연결 관계를 다시 확인하고, 실행하지 않은 테스트는 통과로 적지 않습니다.

새 파일·삭제 파일·이름 변경도 리뷰 대상입니다. 생성물·잠금 파일·바이너리는 별도 대상으로 표시하고 생성 원본·의존성 변경·파일 영향에 맞게 확인합니다. `numstat`에서 줄 수가 없는 바이너리를 0줄 변경으로 간주하거나 큰 파일을 조용히 제외하지 않습니다.

묶음 목록과 중간 메모는 현재 대화나 임시 파일에서 관리하고 세팅 산출물로 저장소 문서에 누적하지 않습니다. PR 본문에는 리뷰 순서·분리하지 못한 이유·중요한 의존 관계만 필요한 만큼 남깁니다.

## 새 저장소에 적용

1. `.github` 파일을 새 저장소에 포함하고 Issues 기능을 활성화합니다. 이슈·PR 양식은 기본 브랜치에 반영되어야 표시됩니다. [이슈 양식 설정](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/configuring-issue-templates-for-your-repository), [PR 양식 설정](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/creating-a-pull-request-template-for-your-repository)
2. 아래 명령으로 라벨을 생성·갱신합니다. JSON 파일 자체는 GitHub가 자동으로 가져오지 않으며, Form의 자동 라벨은 해당 저장소에 라벨이 먼저 있어야 적용됩니다. [이슈 Form 라벨](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/syntax-for-issue-forms)
3. 새 이슈 화면의 6개 양식과 PR 본문 자동 입력을 확인합니다. 공용 양식은 작업 유형 선택과 분류 시 라벨 지정을 확인합니다. [config.yml](../../.github/ISSUE_TEMPLATE/config.yml)은 일반 기여자의 빈 이슈 선택을 숨깁니다. 쓰기 권한 사용자의 빈 이슈나 CLI/API까지 차단하는 검증 장치는 아닙니다. [선택 화면 설정](https://docs.github.com/en/communities/using-templates-to-encourage-useful-issues-and-pull-requests/configuring-issue-templates-for-your-repository)

저장소 루트에서 GitHub CLI 인증과 Python 3을 준비한 뒤 `OWNER/REPO`를 대상 저장소로 바꿔 실행합니다. 대상 저장소의 쓰기 권한이 필요합니다. 같은 이름의 색상·설명만 정의에 맞춰 갱신하고 다른 라벨은 삭제하지 않으므로 반복 적용할 수 있습니다. 실패하면 원인을 해결한 뒤 다시 실행합니다. [라벨 권한](https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/managing-labels), [gh label create](https://cli.github.com/manual/gh_label_create)

```bash
gh auth status
GH_REPO='OWNER/REPO' python3 - <<'PY'
import json
import os
import subprocess
from pathlib import Path

repo = os.environ["GH_REPO"]
if repo == "OWNER/REPO":
    raise SystemExit("GH_REPO를 실제 대상 저장소로 바꿔 주세요.")

labels = json.loads(Path(".github/labels.json").read_text(encoding="utf-8"))
for label in labels:
    subprocess.run([
        "gh", "label", "create", label["name"], "--repo", repo,
        "--color", label["color"], "--description", label["description"], "--force",
    ], check=True)
PY
```

새 모듈 이름마다 라벨을 미리 늘리지 않습니다. 실제 분류에 필요할 때 정의 파일·양식·이 문서를 함께 갱신하고 대상 저장소에 다시 적용합니다.
