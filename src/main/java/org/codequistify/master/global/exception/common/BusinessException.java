package org.codequistify.master.global.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode, HttpStatus httpStatus) {
        super(errorCode.getMessage());
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.detail = "";
    }

    public BusinessException(ErrorCode errorCode, HttpStatus httpStatus, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.detail = cause.getMessage();
    }

    public BusinessException(HttpStatus httpStatus, Throwable cause) {
        super(ErrorCode.UNKNOWN.getMessage(), cause);
        this.httpStatus = httpStatus;
        this.errorCode = ErrorCode.UNKNOWN;
        this.detail = cause.getMessage();
    }

    public BusinessException(BusinessException businessException, HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.errorCode = businessException.getErrorCode();
        this.detail = businessException.getDetail();
    }

    public BusinessException(ErrorCode errorCode, HttpStatus httpStatus, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(errorCode.getMessage(), cause, enableSuppression, writableStackTrace);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.detail = cause.getMessage();
    }

    public BusinessException(RuntimeException exception) {
        if (exception.getClass() == BusinessException.class) {
            BusinessException businessException = (BusinessException) exception;
            this.httpStatus = businessException.getHttpStatus();
            this.errorCode = businessException.getErrorCode();
            this.detail = businessException.getMessage();
        } else {
            this.httpStatus = HttpStatus.BAD_REQUEST;
            this.errorCode = ErrorCode.UNKNOWN;
            this.detail = exception.getMessage();
        }
    }
}
