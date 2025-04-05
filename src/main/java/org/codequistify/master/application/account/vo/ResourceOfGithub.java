package org.codequistify.master.application.account.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceOfGithub(
        @JsonProperty("id") String id,
        @JsonProperty("login") String login,
        @JsonProperty("name") String name
) {
    public OAuthResource toOAuthResource() {
        return new OAuthResource(id, login, name);
    }
}
