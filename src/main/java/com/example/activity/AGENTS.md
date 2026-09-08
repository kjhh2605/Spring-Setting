# Activity 모듈 지침

[루트 지침](../../../../../../AGENTS.md)에 추가 적용합니다. 책임·동작은 [Activity 문서](../../../../../../docs/domain/activity.md)를 읽습니다. 아래 코드 경로는 이 모듈 기준입니다.

- 사용자 데이터는 소유하지 않으며 `UserLookup`으로 조회합니다. user 내부 구현은 import하지 않습니다.
- `adapter/in/event/UserRegisteredListener`는 입력 Port에 위임합니다. 리스너에 비즈니스 로직·user 영속성 접근을 넣지 않습니다.
- 등록 커밋 후 별도 스레드·트랜잭션에서 소비하며 롤백 시 처리하지 않습니다. 로그는 `userId`, `occurredAt`만 기록하고 표시 이름 등 개인 데이터는 추가하지 않습니다.
- 현재 로그 외 영속 모델·자동 재처리는 없습니다. 전달 보장 확장은 [영속성·이벤트 규칙](../../../../../../docs/conventions/persistence-events.md)을 따릅니다.

## 집중 검증

- 조회·후속 처리: `com.example.activity.application.ActivityServiceTest`.
- 리스너 위임: `com.example.activity.adapter.in.event.UserRegisteredListenerTest`.
- 모듈 조립: `com.example.activity.ActivityModuleTest`.
- 모듈 계약·이벤트 실행·HTTP는 [공통 검사 표](../../../../../../docs/conventions/testing.md#아키텍처와-집중-검사)를 따릅니다. 리스너 직접 호출은 비동기·트랜잭션 검증을 대체하지 않습니다.
