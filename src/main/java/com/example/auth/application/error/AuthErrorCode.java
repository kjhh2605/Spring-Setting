package com.example.auth.application.error;

import org.springframework.http.HttpStatus;

import com.example.shared.error.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-001", "사용자를 찾을 수 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-002", "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH-003", "리프레시 토큰 재사용이 감지되었습니다."),
    KAKAO_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-004", "카카오 로그인에 실패했습니다."),
    OAUTH_REQUEST_INVALID(HttpStatus.BAD_REQUEST, "AUTH-005", "로그인 요청이 만료되었거나 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
