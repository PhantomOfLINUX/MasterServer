package org.codequistify.master.global.util;

import org.codequistify.master.global.exception.common.BusinessException;
import org.springframework.http.ResponseEntity;

public record BasicResponse(
        String response,
        String error,
        String detail
) {
    public static BasicResponse of(BusinessException exception) {
        return new BasicResponse(
                exception.getErrorCode().getMessage(),
                exception.getErrorCode().getCode(),
                exception.getDetail()
        );
    }

    public static ResponseEntity<BasicResponse> toResponseEntity(BusinessException exception) {
        return ResponseEntity
                .status(exception.getHttpStatus())
                .body(BasicResponse.of(exception));
    }
}
