package org.codequistify.master.core.domain.authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.codequistify.master.core.domain.authentication.domain.EmailVerificationType;

public record EmailVerificationRequest(
        @Email(message = "4102") String email,
        @NotNull(message = "4101") EmailVerificationType type
) {
}
