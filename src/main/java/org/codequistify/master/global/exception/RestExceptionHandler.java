package org.codequistify.master.global.exception;

import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<BasicResponse> handleApplicationException(ApplicationException exception) {
        LOGGER.info("[ExceptionHandler] Message: {}, Detail: {}", exception.getMessage(), exception.getDetail());

        return BasicResponse.to(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BasicResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = exception.getBindingResult().getAllErrors().stream()
                                       .findAny()
                                       .map(error -> ErrorCode.findByCode(
                                               error.getDefaultMessage()))
                                       .orElse(ErrorCode.UNKNOWN);

        ApplicationException applicationException = new ApplicationException(errorCode, HttpStatus.BAD_REQUEST);
        LOGGER.info("[ExceptionHandler] Message: {}, Detail: {}",
                    applicationException.getMessage(),
                    applicationException.getDetail());

        return ResponseEntity
                .status(applicationException.getHttpStatus())
                .body(BasicResponse.of(applicationException));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BasicResponse> handleRuntimeException(RuntimeException exception) {
        LOGGER.error("[ExceptionHandler] Runtime exception occurred: ", exception);

        ErrorCode errorCode = ErrorCode.FAIL_PROCEED;
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApplicationException applicationException = new ApplicationException(errorCode, status, "서버 내부 오류가 발생했습니다.");
        LOGGER.error("[ExceptionHandler] Message: {}, Detail: {}",
                     applicationException.getMessage(),
                     exception.getMessage());

        return ResponseEntity
                .status(status)
                .body(BasicResponse.of(applicationException));
    }
}
