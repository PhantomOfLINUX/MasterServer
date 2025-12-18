package org.codequistify.master.domain.lab.virtualworkspace.config;

import org.codequistify.master.domain.lab.virtualworkspace.domain.VirtualWorkspacePublicEndpoint;

public final class VirtualWorkspaceExternalEndpoints {
    private static final String WSS_SCHEME = "wss://";

    private VirtualWorkspaceExternalEndpoints() {
    }

    public static String websocketUrl(VirtualWorkspacePublicEndpoint endpoint) {
        return WSS_SCHEME + endpoint.value();
    }
}

