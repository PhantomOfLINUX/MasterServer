package org.codequistify.master.virtualworkspace.domain.model;

import org.codequistify.master.virtualworkspace.domain.vo.SubjectId;
import org.codequistify.master.virtualworkspace.domain.vo.VirtualWorkspaceId;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VirtualWorkspaceTest {

    @Test
    void recreate를_하면_publicId가_갱신되고_lifecycle은_creating이어야한다() {
        VirtualWorkspaceId id = VirtualWorkspaceId.of(PlayerId.of(1L), StageCode.from("S1015"));
        WorkspaceAccessPolicy accessPolicy = WorkspaceAccessPolicy.ownerOnly(SubjectId.from("sub-1"));
        StageSpecSnapshot spec = StageSpecSnapshot.of(StageCode.from("S1015"), "img", 8080, "100m", "128Mi", "/health");
        VirtualWorkspacePublicEndpoint endpoint = VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), "lab.pol.or.kr");

        VirtualWorkspace vw = VirtualWorkspace.creating(id, WorkspacePublicId.from("vw-1"), spec, endpoint, accessPolicy);

        VirtualWorkspace recreated = vw.recreate(
                WorkspacePublicId.from("vw-2"),
                spec,
                VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-2"), "lab.pol.or.kr")
        );

        assertEquals("vw-2", recreated.publicId().value());
        assertEquals(VirtualWorkspaceStatus.CREATING, recreated.lifecycle().status());
        assertEquals(id, recreated.id());
    }
}
