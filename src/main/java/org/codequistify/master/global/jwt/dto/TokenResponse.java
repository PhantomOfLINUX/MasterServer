package org.codequistify.master.global.jwt.dto;

public record TokenResponse(
        String refreshToken,
        String accessToken
) {
}
