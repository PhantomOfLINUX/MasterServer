package org.codequistify.master.global.exception;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BasicResponse> handleEntityNotFoundException(EntityNotFoundException exception){
        LOGGER.info("{} : {}",exception.getClass().getSimpleName(), exception.getMessage());

        return new ResponseEntity<>(
                new BasicResponse(null,exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<BasicResponse> handleEntityExistsException(EntityExistsException exception){
        LOGGER.info("{} : {}",exception.getClass().getSimpleName(), exception.getMessage());

        return new ResponseEntity<>(
                new BasicResponse(null, exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BasicResponse> handleIllegalArgumentException(IllegalArgumentException exception){
        LOGGER.info("{} : {}",exception.getClass().getSimpleName(), exception.getMessage());

        return new ResponseEntity<>(
                new BasicResponse(null, exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BasicResponse> handleIllegalStateException(IllegalStateException exception){
        LOGGER.info("{} : {}",exception.getClass().getSimpleName(), exception.getMessage());

        return new ResponseEntity<>(
                new BasicResponse(null, exception.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<BasicResponse> messagingException(MessagingException exception){
        LOGGER.info("{} : {}",exception.getClass().getSimpleName(), exception.getMessage());

        return new ResponseEntity<>(
                new BasicResponse(null, exception.getMessage()),
                HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<BasicResponse> authenticationServiceException(AuthenticationServiceException exception){
        LOGGER.info("{} : {}",exception.getClass().getSimpleName(), exception.getMessage());

        return new ResponseEntity<>(
                new BasicResponse(null, exception.getMessage()),
                HttpStatus.UNAUTHORIZED);
    }
}
