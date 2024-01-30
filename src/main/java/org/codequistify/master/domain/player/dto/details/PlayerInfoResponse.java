package org.codequistify.master.domain.player.dto.details;

import lombok.NonNull;

public record PlayerInfoResponse(
        @NonNull Long id,
        String email,
        String name,
        Integer level
) {
}
