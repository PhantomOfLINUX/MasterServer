package org.codequistify.master.domain.lab.virtualworkspace.dto;

public record VirtualWorkspaceExistenceResponse(
        String publicId,
        boolean exists
) {
    public static VirtualWorkspaceExistenceResponse of(String publicId, boolean exists) {
        return new VirtualWorkspaceExistenceResponse(publicId, exists);
    }
}

