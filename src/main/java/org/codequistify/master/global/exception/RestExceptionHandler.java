package org.codequistify.master.global.exception;

import org.codequistify.master.global.exception.common.BusinessException;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BasicResponse> handleBusinessException(BusinessException exception) {
        LOGGER.info("[ExceptionHandler] Message: {}, Detail: {}", exception.getMessage(), exception.getDetail());

        return BasicResponse.toResponseEntity(exception);
    }
}
