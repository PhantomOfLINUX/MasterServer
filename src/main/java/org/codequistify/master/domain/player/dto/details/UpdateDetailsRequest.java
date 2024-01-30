package org.codequistify.master.domain.player.dto.details;

import lombok.NonNull;

public record UpdateDetailsRequest(
        @NonNull Long id,
        Integer levelPoint
) {
}
