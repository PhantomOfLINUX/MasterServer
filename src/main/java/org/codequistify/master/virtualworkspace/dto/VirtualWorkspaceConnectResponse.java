package org.codequistify.master.virtualworkspace.dto;

public record VirtualWorkspaceConnectResponse(
        String websocketUrl,
        String publicId
) {
    public static VirtualWorkspaceConnectResponse of(String websocketUrl, String publicId) {
        return new VirtualWorkspaceConnectResponse(websocketUrl, publicId);
    }
}
