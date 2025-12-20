package org.codequistify.master.virtualworkspace.domain.model;

import java.util.Objects;
import java.util.Optional;

public record VirtualWorkspaceRouting(
        VirtualWorkspacePublicEndpoint publicEndpoint,
        Optional<VirtualWorkspaceInternalRoute> internalRoute
) {
    public VirtualWorkspaceRouting {
        Objects.requireNonNull(publicEndpoint, "publicEndpoint must not be null");
        Objects.requireNonNull(internalRoute, "internalRoute must not be null");
    }

    public static VirtualWorkspaceRouting pending(VirtualWorkspacePublicEndpoint publicEndpoint) {
        return new VirtualWorkspaceRouting(publicEndpoint, Optional.empty());
    }

    public static VirtualWorkspaceRouting routed(
            VirtualWorkspacePublicEndpoint publicEndpoint,
            VirtualWorkspaceInternalRoute internalRoute
    ) {
        Objects.requireNonNull(internalRoute, "internalRoute must not be null");
        return new VirtualWorkspaceRouting(publicEndpoint, Optional.of(internalRoute));
    }
}

