package org.codequistify.master.domain.player.dto.details;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String newPassword
) {
}
