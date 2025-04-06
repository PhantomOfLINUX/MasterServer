package org.codequistify.master.application.lab.dto;

public record PShellExistsResponse(
        String uid,
        Long stageId,
        String stageCode,
        Boolean exists
) {
}
