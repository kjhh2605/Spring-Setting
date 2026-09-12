# 현재 아키텍처 결정

| 주제 | 현재 적용 사항 | 근거 |
| --- | --- | --- |
| 기본 구조 | 단일 JAR·Spring Modulith, 루트 공개 계약, 순수 Domain/JPA 분리 | [ADR-001](001-template-architecture.md) |
| Application | 입력 Port의 command/query 분리, 소유 계약별 DTO, 기본 service 패키지 | [ADR-002](002-application-contracts-and-auth-example.md) |
| 예제 모듈 | user 등록·조회·이벤트와 auth 경계 변환 예제 | [ADR-002](002-application-contracts-and-auth-example.md) |
| ADR 관리 | 새 결정은 별도 기록, 대체된 원문에 취소선과 후속 ADR 링크 | [ADR-003](003-adr-lifecycle.md) |
| 자동 커밋·실행 승인 | 작업 브랜치에서 집중 검사 후 단위별 새 커밋, 이력·DB 삭제·푸시·배포는 요약 후 확인 | [ADR-004](004-agent-commits-and-approvals.md) |
| 작업 손실·GitHub 승인 | 작업을 잃는 Git 명령·PR 병합/닫기/삭제·릴리스 게시는 추가 확인, 요청한 PR·이슈 생성/수정/댓글은 자동 | [ADR-005](005-work-loss-and-github-approvals.md) |

대체 이력은 각 ADR 원문에서 확인합니다. 상세 작성·상태·검증 규칙은 [ADR-003](003-adr-lifecycle.md#작성과-변경-규칙)을 따릅니다.
