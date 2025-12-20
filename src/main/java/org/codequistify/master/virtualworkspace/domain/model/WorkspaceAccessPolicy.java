package org.codequistify.master.virtualworkspace.domain.model;

import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;

import java.util.Objects;

public record WorkspaceAccessPolicy(
        SubjectId owner,
        WorkspaceAccessMode mode
) {
    public WorkspaceAccessPolicy {
        Objects.requireNonNull(owner, "owner must not be null");
        Objects.requireNonNull(mode, "mode must not be null");
    }

    public static WorkspaceAccessPolicy ownerOnly(SubjectId owner) {
        return new WorkspaceAccessPolicy(owner, WorkspaceAccessMode.OWNER_ONLY);
    }

    public static WorkspaceAccessPolicy adminOnly(SubjectId owner) {
        return new WorkspaceAccessPolicy(owner, WorkspaceAccessMode.ADMIN_ONLY);
    }
}

