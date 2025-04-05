package org.codequistify.master.application.account.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialLogInRequest(
        @NotBlank String code
) {
}
