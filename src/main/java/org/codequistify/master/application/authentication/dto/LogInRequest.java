package org.codequistify.master.application.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public record LogInRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}
