package org.codequistify.master.application.account.dto;

import jakarta.validation.constraints.NotBlank;
import org.codequistify.master.core.domain.vo.Email;

public record LogInRequest(
        @NotBlank Email email,
        @NotBlank String password
) {
}
