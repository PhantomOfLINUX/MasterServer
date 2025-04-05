package org.codequistify.master.application.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLogInRequest(
        @NotBlank String code
) {
}
