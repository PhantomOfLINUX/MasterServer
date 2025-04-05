package org.codequistify.master.application.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank String name,
        @NotBlank @Email(message = "4101") String email,
        @NotBlank String password
) {
}
