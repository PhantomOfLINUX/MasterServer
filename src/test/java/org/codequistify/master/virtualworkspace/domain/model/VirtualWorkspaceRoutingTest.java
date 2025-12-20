package org.codequistify.master.virtualworkspace.domain.model;

import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualWorkspaceRoutingTest {

    @Test
    void service가_준비되지_않은_경우에는_internalRoute가_비어있어야한다() {
        VirtualWorkspacePublicEndpoint endpoint = VirtualWorkspacePublicEndpoint.of(
                WorkspacePublicId.from("vw-1"),
                "lab.pol.or.kr"
        );

        VirtualWorkspaceRouting routing = VirtualWorkspaceRouting.pending(endpoint);
        assertTrue(routing.internalRoute().isEmpty());
    }

    @Test
    void service가_준비된_경우에는_internalRoute가_존재해야한다() {
        VirtualWorkspacePublicEndpoint endpoint = VirtualWorkspacePublicEndpoint.of(
                WorkspacePublicId.from("vw-1"),
                "lab.pol.or.kr"
        );
        VirtualWorkspaceInternalRoute internalRoute = VirtualWorkspaceInternalRoute.of("default", "svc-name", 8080);

        VirtualWorkspaceRouting routing = VirtualWorkspaceRouting.routed(endpoint, internalRoute);
        assertFalse(routing.internalRoute().isEmpty());
    }
}

