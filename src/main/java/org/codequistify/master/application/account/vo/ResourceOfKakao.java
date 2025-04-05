package org.codequistify.master.application.account.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceOfKakao(
        @JsonProperty("properties") Properties properties
) {
    public record Properties(
            String id,
            String email,
            String nickname
    ) {}

    public OAuthResource toOAuthResource() {
        return new OAuthResource(
                properties.id(),
                properties.email(),
                properties.nickname()
        );
    }
}
