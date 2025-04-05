package org.codequistify.master.application.exception;

import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {
    private final String detail;
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public ApplicationException(ErrorCode errorCode, HttpStatus httpStatus) {
        super(errorCode.getMessage());
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.detail = "";
    }

    public ApplicationException(ErrorCode errorCode, HttpStatus httpStatus, String detail) {
        super(errorCode.getMessage());
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ApplicationException(ErrorCode errorCode, HttpStatus httpStatus, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.detail = cause.getMessage();
    }

    public ApplicationException(HttpStatus httpStatus, Throwable cause) {
        super(ErrorCode.UNKNOWN.getMessage(), cause);
        this.httpStatus = httpStatus;
        this.errorCode = ErrorCode.UNKNOWN;
        this.detail = cause.getMessage();
    }

    public ApplicationException(BusinessException businessException, HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.errorCode = businessException.getErrorCode();
        this.detail = businessException.getDetail();
    }

    public ApplicationException(ErrorCode errorCode,
                                HttpStatus httpStatus,
                                Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(errorCode.getMessage(), cause, enableSuppression, writableStackTrace);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.detail = cause.getMessage();
    }

    public ApplicationException(RuntimeException exception) {
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
