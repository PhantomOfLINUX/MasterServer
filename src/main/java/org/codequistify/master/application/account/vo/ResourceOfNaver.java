package org.codequistify.master.application.account.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceOfNaver(
        @JsonProperty("response") Response response
) {
    public OAuthResource toOAuthResource() {
        return new OAuthResource(
                response.id(),
                response.email(),
                response.name()
        );
    }

    public record Response(
            String id,
            String email,
            String name
    ) {
    }
}
