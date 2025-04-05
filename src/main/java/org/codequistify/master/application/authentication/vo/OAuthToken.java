package org.codequistify.master.application.authentication.vo;

public record OAuthToken(
        String access_token,
        String refresh_token,
        Long expires_in,
        Long refresh_token_expires_in,
        String scope,
        String token_type,
        String id_token
) {
}
