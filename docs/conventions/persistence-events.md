# 영속성·트랜잭션·이벤트

JPA·DB·프로필·트랜잭션·이벤트 흐름을 변경할 때 적용합니다.

## 트랜잭션과 시간

- 변경 UseCase는 `@Transactional`, 조회 UseCase는 `@Transactional(readOnly = true)`를 적용합니다.
- 날짜·시각은 UTC `Instant`, 테스트 가능한 시간은 주입받은 `Clock`을 사용합니다.
- Entity에는 공개 `@Setter`를 두지 않고 의미 있는 도메인 메서드로 상태를 변경합니다.

## 트랜잭션 이벤트

- 완료 이벤트는 변경 트랜잭션 안에서 발행하고 `@ApplicationModuleListener`로 커밋 후 소비합니다. 롤백한 트랜잭션의 이벤트는 소비하지 않습니다.
- `shared.internal.config.AsyncConfig`의 `@EnableAsync`와 Boot의 TaskExecutor를 사용합니다. 리스너는 발행자와 다른 스레드·별도 트랜잭션에서 실행합니다.
- 발생 시각은 발행 시점의 `Clock` 값이며 커밋 시각이 아닙니다. 후속 처리 완료는 API 응답과 독립적이고, 소비 실패가 이미 커밋된 등록을 되돌리지 않습니다.
- 현재 후속 처리는 로그뿐이며 영속 Event Publication Registry·자동 재처리 보장은 없습니다. 전달 보장이 필요한 확장은 저장소·재처리·멱등성과 ADR을 함께 설계합니다.
- 실제 커밋·롤백·소비 스레드를 통합 테스트로 검증합니다. 테스트 전체를 트랜잭션으로 감싸지 않습니다.

## JPA와 스키마

- JPA Entity는 Domain 모델과 분리하고 Persistence Adapter에서 변환합니다. 기본 생성자는 `protected`, 연관관계는 지연 로딩이 기본입니다.
- QueryDSL 생성물은 `build/generated/querydsl`에 두고 직접 수정하지 않습니다.
- 패키징된 설정은 활성 프로필을 기본 선택하지 않습니다. 환경에서 `SPRING_PROFILES_ACTIVE`를 지정합니다.
- 공통·`prod`의 `ddl-auto`는 `none`, `create-drop`은 로컬 예제·테스트에만 허용합니다. 로컬 DB에 보존할 데이터를 넣지 않습니다.
- 운영은 `SPRING_PROFILES_ACTIVE=prod`로 실행합니다. DB 정보 `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`는 기본값 없이 받아 누락 시 시작에 실패하게 합니다.
- `prod,local`이 함께 지정되어도 로컬 설정이 활성화되지 않도록 프로필 표현식으로 보호합니다. 운영 OpenAPI UI는 비활성화합니다.
- 프로필 설정은 `application-{profile}.yml`로 분리하고 공통 설정을 중복하지 않습니다.
- 현재 마이그레이션 도구는 없습니다. 운영 배포 전에 마이그레이션 전략을 ADR로 결정합니다.

선택 배경: [영속성과 이벤트 결정](../adr/001-template-architecture.md).
