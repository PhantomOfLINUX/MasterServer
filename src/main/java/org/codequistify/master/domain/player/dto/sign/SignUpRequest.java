package org.codequistify.master.domain.player.dto.sign;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank String name,
        @NotBlank String email,
        @NotBlank String password
) {
}
