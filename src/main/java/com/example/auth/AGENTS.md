# Auth 모듈 지침

[루트 지침](../../../../../../AGENTS.md)에 추가 적용합니다. 구현 범위·공개 계약·흐름은 [Auth 문서](../../../../../../docs/domain/auth.md)의 관련 절이 원본입니다.

- 예제 subject나 사용자 식별자만으로 인증 성공·토큰·세션을 만들지 않습니다.
- user의 공개 조회 결과는 `adapter/out/user`, 공개 이벤트는 `adapter/in/event`에서 auth 소유 값으로 변환합니다. 이벤트 입력은 auth의 입력 Port에 위임합니다. user 내부 타입·Repository를 참조하거나 Application·Domain에서 user 타입을 참조하지 않습니다.
- 등록 커밋 후 별도 스레드·트랜잭션에서 소비하며 롤백 시 처리하지 않습니다. 로그는 `userId`, `occurredAt`만 기록합니다.
- 사용자 원본은 user가 소유합니다. 영속 모델·재처리 확장은 [영속성·이벤트](../../../../../../docs/conventions/persistence-events.md#트랜잭션-이벤트)에 따라 설계하고 Auth 문서에 반영합니다.

## 집중 검증

- Domain: `com.example.auth.domain.AuthSubjectTest`.
- 조회: `com.example.auth.application.service.GetAuthSubjectServiceTest`.
- 경계 변환: `com.example.auth.adapter.out.user.UserSubjectAdapterTest`, `com.example.auth.adapter.in.event.UserRegisteredListenerTest`.
- 모듈 조립: `com.example.auth.AuthModuleTest`.
- HTTP·OpenAPI·이벤트·모듈 경계는 [공통 검사 표](../../../../../../docs/conventions/testing.md)를 따릅니다. 실제 커밋·롤백·비동기 검증을 리스너 직접 호출로 대체하지 않습니다.
