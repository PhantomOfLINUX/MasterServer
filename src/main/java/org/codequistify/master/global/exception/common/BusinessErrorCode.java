package org.codequistify.master.global.exception.common;

import lombok.Getter;

@Getter
public enum BusinessErrorCode {
    // 알려지지 않은 문제
    UNKNOWN("알려지지 않은 문제가 발생하였습니다.", "4000_UNKNOWN_ERROR"),
    // 공백값 존재
    BLANK_ARGUMENT("비어있는 값이 존재합니다.", "4001_BLANK_ARGUMENT_ERROR"),
    // 이메일 형식 위반
    INVALID_EMAIL_FORMAT("이메일 형식이 올바르지 않습니다.", "4002_INVALID_EMAIL_FORMAT"),
    // 비밀번호 조건 위반
    PASSWORD_POLICY_VIOLATION("비밀번호 조건을 충족하지 않습니다.", "4003_PASSWORD_POLICY_VIOLATION"),
    // 이메일 중복
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다.", "4004_EMAIL_ALREADY_EXISTS"),
    // oauth 통신 실패
    OAUTH_COMMUNICATION_FAILURE("OAuth 통신에 실패하였습니다.", "4005_OAUTH_COMMUNICATION_FAILURE"),
    // 이메일, 비밀번호 틀림
    INVALID_EMAIL_OR_PASSWORD("이메일 또는 비밀번호가 틀립니다.", "4006_INVALID_EMAIL_OR_PASSWORD"),
    // 존재하지 않는 플레이어
    PLAYER_NOT_FOUND("존재하지 않는 플레이어입니다.", "4007_PLAYER_NOT_FOUND"),
    // 올바르지 않은 oauth code
    INVALID_OAUTH_CODE("올바르지 않은 OAuth code입니다.", "4008_INVALID_OAUTH_CODE"),
    // 올바르지 않은 token
    INVALID_TOKEN("올바르지 않은 토큰입니다.", "4009_INVALID_TOKEN"),
    // 만료된 엑세스 token
    EXPIRED_ACCESS_TOKEN("만료된 엑세스 토큰입니다.", "4010_EXPIRED_ACCESS_TOKEN"),
    // 만료된 refresh token
    EXPIRED_REFRESH_TOKEN("만료된 리프레시 토큰입니다.", "4011_EXPIRED_REFRESH_TOKEN"),
    // 권한이 부족한 token
    INSUFFICIENT_PLAYER_PERMISSION("권한이 부족한 플레이어입니다.", "4012_INSUFFICIENT_TOKEN_PERMISSION");

    private final String message;
    private final String code;

    BusinessErrorCode(String message, String code) {
        this.message = message;
        this.code = code;
    }
}
