package org.codequistify.master.domain.player.dto.sign;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LogInRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
