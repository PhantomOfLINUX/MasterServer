package org.codequistify.master.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 알려지지 않은 문제
    UNKNOWN("알려지지 않은 문제가 발생하였습니다.", "4000_UNKNOWN_ERROR"),
    // 입력 값 오류
    BLANK_ARGUMENT("비어있는 값이 존재합니다.", "4101_BLANK_ARGUMENT_ERROR"),
    INVALID_EMAIL_FORMAT("이메일 형식이 올바르지 않습니다.", "4102_INVALID_EMAIL_FORMAT_ERROR"),
    PASSWORD_POLICY_VIOLATION("비밀번호 조건을 충족하지 않습니다.", "4103_PASSWORD_POLICY_VIOLATION_ERROR"),
    INVALID_SEARCH_CRITERIA("검색 조건을 잘못 설정하였습니다.", "4104_INVALID_SEARCH_CRITERIA_ERROR"),
    // 계정 관련 오류
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다.", "4201_EMAIL_ALREADY_EXISTS_ERROR"),
    INVALID_EMAIL_OR_PASSWORD("이메일 또는 비밀번호가 틀립니다.", "4201_INVALID_EMAIL_OR_PASSWORD_ERROR"),
    PLAYER_NOT_FOUND("존재하지 않는 플레이어입니다.", "4201_PLAYER_NOT_FOUND_ERROR"),
    // OAuth 및 토큰 오류
    OAUTH_COMMUNICATION_FAILURE("OAuth 통신에 실패하였습니다.", "4301_OAUTH_COMMUNICATION_FAILURE_ERROR"),
    INVALID_OAUTH_CODE("올바르지 않은 OAuth code입니다.", "4302_INVALID_OAUTH_CODE_ERROR"),
    INVALID_TOKEN("올바르지 않은 토큰입니다.", "4303_INVALID_TOKEN_ERROR"),
    EXPIRED_ACCESS_TOKEN("만료된 엑세스 토큰입니다.", "4304_EXPIRED_ACCESS_TOKEN_ERROR"),
    EXPIRED_REFRESH_TOKEN("만료된 리프레시 토큰입니다.", "4305_EXPIRED_REFRESH_TOKEN_ERROR"),
    TAMPERED_TOKEN_SIGNATURE("서명이 변형된 토큰입니다.", "4306_TAMPERED_TOKEN_SIGNATURE_ERROR"),
    EMPTY_TOKEN_PROVIDED("요청에 토큰이 비어있습니다.", "4307_EMPTY_TOKEN_PROVIDED_ERROR"),
    INSUFFICIENT_PLAYER_PERMISSION("권한이 부족한 플레이어입니다.", "4308_INSUFFICIENT_TOKEN_PERMISSION_ERROR"),
    // 메일 전송 오류
    EMAIL_SENDING_FAILURE("메일 전송에 실패하였습니다.", "4309_EMAIL_SENDING_FAILURE_ERROR");

    private final String message;
    private final String code;

    ErrorCode(String message, String code) {
        this.message = message;
        this.code = code;
    }
}
