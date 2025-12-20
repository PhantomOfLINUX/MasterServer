package org.codequistify.master.virtualworkspace.presentation.dto;

public record VirtualWorkspaceSummaryResponse(
        String publicId,
        String status,
        boolean exists
) {
    public static VirtualWorkspaceSummaryResponse of(String publicId, String status, boolean exists) {
        return new VirtualWorkspaceSummaryResponse(publicId, status, exists);
    }
}
