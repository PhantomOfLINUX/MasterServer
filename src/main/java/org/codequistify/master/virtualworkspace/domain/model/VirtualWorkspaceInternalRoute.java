package org.codequistify.master.virtualworkspace.domain.model;

import java.util.Objects;

public record VirtualWorkspaceInternalRoute(
        String namespace,
        String serviceName,
        int servicePort
) {
    public VirtualWorkspaceInternalRoute {
        namespace = requireNonBlank(namespace, "namespace must not be blank");
        serviceName = requireNonBlank(serviceName, "serviceName must not be blank");
        if (servicePort <= 0 || servicePort > 65535) {
            throw new IllegalArgumentException("servicePort must be between 1 and 65535");
        }
    }

    public static VirtualWorkspaceInternalRoute of(String namespace, String serviceName, int servicePort) {
        return new VirtualWorkspaceInternalRoute(namespace, serviceName, servicePort);
    }

    private static String requireNonBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }
}

