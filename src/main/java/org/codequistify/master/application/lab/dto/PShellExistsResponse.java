package org.codequistify.master.application.lab.dto;

import org.codequistify.master.core.domain.player.model.PolId;

public record PShellExistsResponse(
        PolId uid,
        Long stageId,
        String stageCode,
        Boolean exists
) {
}
