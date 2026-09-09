# Activity 모듈

## 책임

- `user`의 공개 조회 계약으로 사용자 관점의 활동 설명을 만듭니다.
- 사용자 등록 완료 이벤트를 수신해 후속 활동을 처리합니다.

## 현재 기능

- `GET /api/v1/activities/users/{userId}`는 `Activity for {displayName}`을 반환합니다.
- 사용자가 없으면 `application.error.ActivityErrorCode.USER_NOT_FOUND`를 `BusinessException`으로 전달하고 HTTP 404와 기존 `ACTIVITY-404` 오류 코드를 반환합니다.
- `UserRegistered` 수신 시 `userId`, `occurredAt`을 포함한 로그만 남깁니다.
- 등록 트랜잭션 커밋 후 발행자와 다른 스레드 및 별도 트랜잭션에서 처리하며, 롤백한 등록은 기록하지 않습니다.
- 영속 이벤트 저장소가 없으므로 프로세스 종료나 소비 실패 시 자동 재처리를 보장하지 않습니다.

## 경계

이 모듈은 사용자 JPA Entity나 Repository를 참조하지 않습니다. 즉시 필요한 사용자 정보는 `UserLookup`으로 조회하고, 등록 완료 후속 처리는 `UserRegistered`에 반응합니다. 현재는 활동 데이터를 영속화하지 않습니다.
