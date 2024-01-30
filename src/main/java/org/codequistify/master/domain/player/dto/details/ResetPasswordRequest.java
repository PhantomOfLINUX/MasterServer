package org.codequistify.master.domain.player.dto.details;

import lombok.NonNull;

public record ResetPasswordRequest(
        @NonNull Long id,
        @NonNull String newPassword
) {
}
