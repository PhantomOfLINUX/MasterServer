package org.codequistify.master.application.account.vo;

public record OAuthData(
        OAuthToken token,
        OAuthResource resource
) {
    public static OAuthData of(OAuthToken token, OAuthResource resource) {
        return new OAuthData(token, resource);
    }
}
