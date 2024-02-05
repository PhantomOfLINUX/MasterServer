package org.codequistify.master.domain.player.dto.details;

import lombok.NonNull;

public record PlayerInfoResponse(
        @NonNull String uid,
        String email,
        String name,
        Integer level
) {
}
