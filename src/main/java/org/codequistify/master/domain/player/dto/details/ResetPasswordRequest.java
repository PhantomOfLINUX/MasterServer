package org.codequistify.master.domain.player.dto.details;

import lombok.NonNull;

public record ResetPasswordRequest(
        @NonNull String newPassword
) {
}
