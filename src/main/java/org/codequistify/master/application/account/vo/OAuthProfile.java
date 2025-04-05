package org.codequistify.master.application.account.vo;

public record OAuthProfile(
        OAuthToken token,
        OAuthResource resource
) {
    public static OAuthProfile of(OAuthToken token, OAuthResource resource) {
        return new OAuthProfile(token, resource);
    }
}
