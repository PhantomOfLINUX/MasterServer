package org.codequistify.master.core.domain.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLogInRequest(
        @NotBlank String code
) {
}
