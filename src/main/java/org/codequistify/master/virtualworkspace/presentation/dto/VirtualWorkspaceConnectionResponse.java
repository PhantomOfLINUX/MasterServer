package org.codequistify.master.virtualworkspace.presentation.dto;

public record VirtualWorkspaceConnectionResponse(
        String publicId,
        String publicHost,
        String websocketUrl
) {
    public static VirtualWorkspaceConnectionResponse of(String publicId, String publicHost, String websocketUrl) {
        return new VirtualWorkspaceConnectionResponse(publicId, publicHost, websocketUrl);
    }
}
