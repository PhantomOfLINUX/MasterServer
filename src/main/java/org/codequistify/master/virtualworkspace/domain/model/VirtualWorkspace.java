package org.codequistify.master.virtualworkspace.domain.model;

import java.util.Objects;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;

public record VirtualWorkspace(
    VirtualWorkspaceId id,
    WorkspacePublicId publicId,
    VirtualWorkspaceLifecycle lifecycle,
    StageSpecSnapshot specSnapshot,
    VirtualWorkspaceRouting routing,
    WorkspaceAccessPolicy accessPolicy) {
  public VirtualWorkspace {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(publicId, "publicId must not be null");
    Objects.requireNonNull(lifecycle, "lifecycle must not be null");
    Objects.requireNonNull(specSnapshot, "specSnapshot must not be null");
    Objects.requireNonNull(routing, "routing must not be null");
    Objects.requireNonNull(accessPolicy, "accessPolicy must not be null");
  }

  public static VirtualWorkspace creating(
      VirtualWorkspaceId id,
      WorkspacePublicId publicId,
      StageSpecSnapshot specSnapshot,
      VirtualWorkspacePublicEndpoint publicEndpoint,
      WorkspaceAccessPolicy accessPolicy) {
    return new VirtualWorkspace(
        id,
        publicId,
        VirtualWorkspaceLifecycle.creating(),
        specSnapshot,
        VirtualWorkspaceRouting.pending(publicEndpoint),
        accessPolicy);
  }

  public VirtualWorkspace recreate(
      WorkspacePublicId newPublicId,
      StageSpecSnapshot newSpecSnapshot,
      VirtualWorkspacePublicEndpoint newPublicEndpoint) {
    Objects.requireNonNull(newPublicId, "newPublicId must not be null");
    Objects.requireNonNull(newSpecSnapshot, "newSpecSnapshot must not be null");
    Objects.requireNonNull(newPublicEndpoint, "newPublicEndpoint must not be null");

    return new VirtualWorkspace(
        id,
        newPublicId,
        VirtualWorkspaceLifecycle.creating(),
        newSpecSnapshot,
        VirtualWorkspaceRouting.pending(newPublicEndpoint),
        accessPolicy);
  }

  public VirtualWorkspace markRunning(VirtualWorkspaceInternalRoute internalRoute) {
    Objects.requireNonNull(internalRoute, "internalRoute must not be null");

    return new VirtualWorkspace(
        id,
        publicId,
        VirtualWorkspaceLifecycle.running(),
        specSnapshot,
        VirtualWorkspaceRouting.routed(routing.publicEndpoint(), internalRoute),
        accessPolicy);
  }
}
