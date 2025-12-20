package org.codequistify.master.virtualworkspace.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.junit.jupiter.api.Test;

class VirtualWorkspaceTest {

  @Test
  void creating으로_생성하면_초기_상태가_설정되어야한다() {
    VirtualWorkspaceId id = VirtualWorkspaceId.of(PlayerId.of(1L), StageCode.from("S1001"));
    WorkspaceAccessPolicy accessPolicy = WorkspaceAccessPolicy.ownerOnly(SubjectId.from("sub-1"));
    StageSpecSnapshot spec =
        StageSpecSnapshot.of(StageCode.from("S1001"), "img", 8080, "100m", "128Mi", "/health");
    VirtualWorkspacePublicEndpoint endpoint =
        VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), "lab.pol.or.kr");

    VirtualWorkspace vw =
        VirtualWorkspace.creating(id, WorkspacePublicId.from("vw-1"), spec, endpoint, accessPolicy);

    assertEquals(VirtualWorkspaceStatus.CREATING, vw.lifecycle().status());
    assertTrue(vw.routing().internalRoute().isEmpty());
    assertEquals(endpoint, vw.routing().publicEndpoint());
  }

  @Test
  void recreate를_하면_publicId가_갱신되고_lifecycle은_creating이어야한다() {
    VirtualWorkspaceId id = VirtualWorkspaceId.of(PlayerId.of(1L), StageCode.from("S1001"));
    WorkspaceAccessPolicy accessPolicy = WorkspaceAccessPolicy.ownerOnly(SubjectId.from("sub-1"));
    StageSpecSnapshot spec =
        StageSpecSnapshot.of(StageCode.from("S1001"), "img", 8080, "100m", "128Mi", "/health");
    VirtualWorkspacePublicEndpoint endpoint =
        VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), "lab.pol.or.kr");

    VirtualWorkspace vw =
        VirtualWorkspace.creating(id, WorkspacePublicId.from("vw-1"), spec, endpoint, accessPolicy);

    VirtualWorkspace recreated = vw.recreate(
        WorkspacePublicId.from("vw-2"),
        spec,
        VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-2"), "lab.pol.or.kr"));

    assertEquals("vw-2", recreated.publicId().value());
    assertEquals(VirtualWorkspaceStatus.CREATING, recreated.lifecycle().status());
    assertEquals(id, recreated.id());
  }

  @Test
  void markRunning을_하면_running_상태와_internalRoute가_설정되어야한다() {
    VirtualWorkspaceId id = VirtualWorkspaceId.of(PlayerId.of(1L), StageCode.from("S1001"));
    WorkspaceAccessPolicy accessPolicy = WorkspaceAccessPolicy.ownerOnly(SubjectId.from("sub-1"));
    StageSpecSnapshot spec =
        StageSpecSnapshot.of(StageCode.from("S1001"), "img", 8080, "100m", "128Mi", "/health");
    VirtualWorkspacePublicEndpoint endpoint =
        VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), "lab.pol.or.kr");
    VirtualWorkspaceInternalRoute internalRoute =
        VirtualWorkspaceInternalRoute.of("default", "svc-name", 8080);

    VirtualWorkspace vw =
        VirtualWorkspace.creating(id, WorkspacePublicId.from("vw-1"), spec, endpoint, accessPolicy);

    VirtualWorkspace running = vw.markRunning(internalRoute);

    assertEquals(VirtualWorkspaceStatus.RUNNING, running.lifecycle().status());
    assertTrue(running.routing().internalRoute().isPresent());
    assertEquals(internalRoute, running.routing().internalRoute().get());
  }
}
