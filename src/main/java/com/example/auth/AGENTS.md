# Auth 모듈 지침

[루트 지침](../../../../../../AGENTS.md)에 추가 적용합니다. [Auth 문서](../../../../../../docs/domain/auth.md)를 읽습니다.

- 실제 인증 대신 모듈 구조를 보여주는 예제입니다. 사용자 식별자만으로 인증 성공·토큰·세션을 만들지 않습니다.
- `adapter/out/user/UserSubjectAdapter`에서 user의 공개 `UserLookup` 결과를 auth 소유 `AuthSubject`로 변환합니다. user 내부 타입과 Repository는 참조하지 않습니다.
- `adapter/in/event/UserRegisteredListener`는 공개 이벤트를 auth 소유 Command로 변환하고 입력 Port에 위임합니다. Application과 Domain에서 user 타입을 참조하지 않습니다.
- 등록 커밋 후 별도 스레드·트랜잭션에서 소비하며 롤백 시 처리하지 않습니다. 로그는 `userId`, `occurredAt`만 기록합니다.
- auth는 사용자 원본·별도 테이블·영속 이벤트 저장소를 소유하지 않습니다. 재처리 확장은 [영속성·이벤트](../../../../../../docs/conventions/persistence-events.md)를 따릅니다.

## 집중 검증

- Domain: `com.example.auth.domain.AuthSubjectTest`.
- 조회: `com.example.auth.application.service.GetAuthSubjectServiceTest`.
- 경계 변환: `com.example.auth.adapter.out.user.UserSubjectAdapterTest`, `com.example.auth.adapter.in.event.UserRegisteredListenerTest`.
- 모듈 조립: `com.example.auth.AuthModuleTest`.
- HTTP·OpenAPI·이벤트·모듈 경계는 [공통 검사 표](../../../../../../docs/conventions/testing.md)를 따릅니다. 실제 커밋·롤백·비동기 검증을 리스너 직접 호출로 대체하지 않습니다.
