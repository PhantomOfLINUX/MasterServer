package org.codequistify.master.application.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
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

    public ApplicationException(ApplicationException applicationException, HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.errorCode = applicationException.getErrorCode();
        this.detail = applicationException.getDetail();
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
        if (exception.getClass() == ApplicationException.class) {
            ApplicationException applicationException = (ApplicationException) exception;
            this.httpStatus = applicationException.getHttpStatus();
            this.errorCode = applicationException.getErrorCode();
            this.detail = applicationException.getMessage();
        } else {
            this.httpStatus = HttpStatus.BAD_REQUEST;
            this.errorCode = ErrorCode.UNKNOWN;
            this.detail = exception.getMessage();
        }
    }
}
