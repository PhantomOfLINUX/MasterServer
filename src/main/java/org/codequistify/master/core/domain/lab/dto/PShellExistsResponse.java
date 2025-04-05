package org.codequistify.master.core.domain.lab.dto;

public record PShellExistsResponse(
        String uid,
        Long stageId,
        String stageCode,
        Boolean exists
) {
}
