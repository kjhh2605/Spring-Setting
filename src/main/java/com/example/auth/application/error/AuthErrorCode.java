package com.example.auth.application.error;

import org.springframework.http.HttpStatus;

import com.example.shared.error.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-001", "사용자를 찾을 수 없습니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-002", "지원하지 않는 로그인 제공자입니다."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-003", "유효하지 않은 소셜 토큰입니다."),
    SOCIAL_PROVIDER_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "AUTH-004", "소셜 로그인 제공자에 연결할 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-005", "만료되었거나 유효하지 않은 토큰입니다."),
    INVALID_DEV_SECRET(HttpStatus.UNAUTHORIZED, "AUTH-006", "개발용 인증키가 올바르지 않습니다."),
    SESSION_STORE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AUTH-007", "인증 세션 저장소를 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
