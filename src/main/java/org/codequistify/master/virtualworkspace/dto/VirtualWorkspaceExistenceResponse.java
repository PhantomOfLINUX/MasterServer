package org.codequistify.master.virtualworkspace.dto;

public record VirtualWorkspaceExistenceResponse(
        String publicId,
        boolean exists
) {
    public static VirtualWorkspaceExistenceResponse of(String publicId, boolean exists) {
        return new VirtualWorkspaceExistenceResponse(publicId, exists);
    }
}
