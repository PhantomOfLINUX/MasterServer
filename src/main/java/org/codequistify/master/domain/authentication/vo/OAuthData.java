package org.codequistify.master.domain.authentication.vo;

public record OAuthData(
        OAuthTokenVO token,
        OAuthResourceVO resource
) {
    public static OAuthData of(OAuthTokenVO token, OAuthResourceVO resource) {
        return new OAuthData(token, resource);
    }
}
