package org.codequistify.master.global.util;

import org.codequistify.master.application.exception.ApplicationException;
import org.springframework.http.ResponseEntity;

public record BasicResponse(
        String response,
        String error,
        String detail
) {
    public static BasicResponse of(ApplicationException exception) {
        return new BasicResponse(
                exception.getErrorCode().getMessage(),
                exception.getErrorCode().getCode(),
                exception.getDetail()
        );
    }

    public static BasicResponse of(String response) {
        return new BasicResponse(
                response,
                "",
                ""
        );
    }

    public static ResponseEntity<BasicResponse> to(ApplicationException exception) {
        return ResponseEntity
                .status(exception.getHttpStatus())
                .body(BasicResponse.of(exception));
    }
}
