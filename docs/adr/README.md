# 현재 아키텍처 결정

| 주제 | 현재 적용 사항 | 근거 |
| --- | --- | --- |
| 기본 구조 | 단일 JAR·Spring Modulith, 루트 공개 계약, 순수 Domain/JPA 분리 | [ADR-001](001-template-architecture.md) |
| Application | 입력 Port의 command/query 분리, 소유 계약별 DTO, 기본 service 패키지 | [ADR-002](002-application-contracts-and-auth-example.md) |
| 예제 모듈 | user 등록·조회·이벤트와 auth 경계 변환 예제 | [ADR-002](002-application-contracts-and-auth-example.md) |
| 소셜 인증 | 카카오 전략/resolver, user 소셜 연결, JWT·Redis RTR, dev 발급 | [ADR-006](006-social-login-and-refresh-rotation.md) |
| ADR 관리 | 새 결정은 별도 기록, 대체된 원문에 취소선과 후속 ADR 링크 | [ADR-003](003-adr-lifecycle.md) |

대체 이력은 각 ADR 원문에서 확인합니다. 상세 작성·상태·검증 규칙은 [ADR-003](003-adr-lifecycle.md#작성과-변경-규칙)을 따릅니다.
