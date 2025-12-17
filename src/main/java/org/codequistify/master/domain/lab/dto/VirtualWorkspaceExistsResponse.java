package org.codequistify.master.domain.lab.dto;

public record VirtualWorkspaceExistsResponse(
        String uid,
        Long stageId,
        String stageCode,
        Boolean exists
) {
}
