package org.codequistify.master.domain.authentication.vo;

public record OAuthData(
        OAuthToken token,
        OAuthResource resource
) {
    public static OAuthData of(OAuthToken token, OAuthResource resource) {
        return new OAuthData(token, resource);
    }
}
