package org.codequistify.master.core.domain.player.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(
        String rawPassword,
        @NotBlank(message = "4101") String password
) {
}
