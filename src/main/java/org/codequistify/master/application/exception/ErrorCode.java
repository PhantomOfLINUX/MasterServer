package org.codequistify.master.application.exception;

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
    PROFANITY_IN_NAME("이름에 비속어가 포함되어 있습니다.", "4105_PROFANITY_IN_NAME_ERROR"),
    DUPLICATE_NAME("중복된 이름입니다.", "4106_DUPLICATE_NAME_ERROR"),

    // 계정 관련 오류
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다.", "4201_EMAIL_ALREADY_EXISTS_ERROR"),
    EMAIL_ALREADY_EXISTS_OTHER_AUTH("이미 다른 방식으로 인증되어 있는 이메일입니다.", "4202_ EMAIL_ALREADY_EXISTS_OTHER_AUTH_ERROR"),
    INVALID_EMAIL_OR_PASSWORD("이메일 또는 비밀번호가 틀립니다.", "4203_INVALID_EMAIL_OR_PASSWORD_ERROR"),
    PLAYER_NOT_FOUND("존재하지 않는 플레이어입니다.", "4204_PLAYER_NOT_FOUND_ERROR"),
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
    EMAIL_SENDING_FAILURE("메일 전송에 실패하였습니다.", "4401_EMAIL_SENDING_FAILURE_ERROR"),
    EMAIL_VERIFIED_FAILURE("메일 인증에 실패하였습니다.", "4402_EMAIL_VERIFIED_FAILURE_ERROR"),
    // 스테이지 관련 오류
    STAGE_NOT_FOUND("존재하지 않는 단계입니다.", "4501_STAGE_NOT_FOUND_ERROR"),
    QUESTION_NOT_FOUND("존재하지 않는 문항입니다.", "4502_QUESTION_NOT_FOUND_ERROR"),
    STAGE_PROGRESS_NOT_FOUND("진행 상황이 존재하지 않습니다.", "4503_TAGE_PROGRESS_NOT_FOUND_ERROR"),
    // 터미널 관련 오류
    INVALID_HEADER("올바르지 않은 헤더 정보입니다.", "4601_INVALID_HEADER_ERROR"),
    PSHELL_NOT_FOUND("존재하지 PShell 정보입니다", "4602_PSHELL_NOT_FOUND"),
    PSHELL_CREATE_FAILED("PShell 생성에 실패하였습니다", "46023_PSHELL_CREATE_FAILED"),
    //
    FAIL_PROCEED("프로세스 실행중 문제가 발생하였습니다.", "5000_FAIL_PROCESSED_ERROR"),
    // 정상
    SUCCESS("SUCCESS", "2000_SUCCESS_OK");

    private final String code;
    private final String message;

    ErrorCode(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public static ErrorCode findByCode(String code) {
        for (ErrorCode value : ErrorCode.values()) {
            if (value.code.startsWith(code)) {
                return value;
            }
        }
        return ErrorCode.UNKNOWN;
    }
}
