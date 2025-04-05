package org.codequistify.master.global.exception;

import org.codequistify.master.global.exception.domain.BusinessException;
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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BasicResponse> handleBusinessException(BusinessException exception) {
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

        BusinessException businessException = new BusinessException(errorCode, HttpStatus.BAD_REQUEST);
        LOGGER.info("[ExceptionHandler] Message: {}, Detail: {}",
                    businessException.getMessage(),
                    businessException.getDetail());

        return ResponseEntity
                .status(businessException.getHttpStatus())
                .body(BasicResponse.of(businessException));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BasicResponse> handleRuntimeException(RuntimeException exception) {
        LOGGER.error("[ExceptionHandler] Runtime exception occurred: ", exception);

        ErrorCode errorCode = ErrorCode.FAIL_PROCEED;
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        BusinessException businessException = new BusinessException(errorCode, status, "서버 내부 오류가 발생했습니다.");
        LOGGER.error("[ExceptionHandler] Message: {}, Detail: {}",
                     businessException.getMessage(),
                     exception.getMessage());

        return ResponseEntity
                .status(status)
                .body(BasicResponse.of(businessException));
    }
}
