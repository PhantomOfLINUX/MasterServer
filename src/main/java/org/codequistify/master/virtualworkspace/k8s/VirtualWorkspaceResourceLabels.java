package org.codequistify.master.domain.lab.virtualworkspace.k8s;

import org.codequistify.master.domain.lab.virtualworkspace.domain.VirtualWorkspace;
import org.codequistify.master.global.data.Label;
import org.codequistify.master.global.data.Labels;

public final class VirtualWorkspaceResourceLabels {
    private VirtualWorkspaceResourceLabels() {
    }

    public static Labels standard(VirtualWorkspace workspace) {
        return Labels.of(
                Label.of("app", "virtual-workspace"),
                Label.of("playerId", workspace.id().playerId().value().toString()),
                Label.of("stageCode", workspace.id().stageCode().lowercase()),
                Label.of("publicId", workspace.publicId().value())
        );
    }
}

