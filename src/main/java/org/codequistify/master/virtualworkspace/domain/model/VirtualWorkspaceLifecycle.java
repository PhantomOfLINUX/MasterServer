package org.codequistify.master.virtualworkspace.domain.model;

import java.util.Objects;

public record VirtualWorkspaceLifecycle(VirtualWorkspaceStatus status) {
    public VirtualWorkspaceLifecycle {
        Objects.requireNonNull(status, "status must not be null");
    }

    public static VirtualWorkspaceLifecycle creating() {
        return new VirtualWorkspaceLifecycle(VirtualWorkspaceStatus.CREATING);
    }

    public static VirtualWorkspaceLifecycle running() {
        return new VirtualWorkspaceLifecycle(VirtualWorkspaceStatus.RUNNING);
    }

    public static VirtualWorkspaceLifecycle idle() {
        return new VirtualWorkspaceLifecycle(VirtualWorkspaceStatus.IDLE);
    }

    public static VirtualWorkspaceLifecycle terminating() {
        return new VirtualWorkspaceLifecycle(VirtualWorkspaceStatus.TERMINATING);
    }

    public static VirtualWorkspaceLifecycle removed() {
        return new VirtualWorkspaceLifecycle(VirtualWorkspaceStatus.REMOVED);
    }

    public boolean isActive() {
        return status == VirtualWorkspaceStatus.CREATING
                || status == VirtualWorkspaceStatus.RUNNING
                || status == VirtualWorkspaceStatus.IDLE;
    }
}

