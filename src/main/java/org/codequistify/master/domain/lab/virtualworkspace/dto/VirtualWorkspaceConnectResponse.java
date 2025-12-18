package org.codequistify.master.domain.lab.virtualworkspace.dto;

public record VirtualWorkspaceConnectResponse(
        String websocketUrl,
        String publicId
) {
    public static VirtualWorkspaceConnectResponse of(String websocketUrl, String publicId) {
        return new VirtualWorkspaceConnectResponse(websocketUrl, publicId);
    }
}

