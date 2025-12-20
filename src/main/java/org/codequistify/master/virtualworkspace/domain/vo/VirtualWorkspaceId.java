package org.codequistify.master.virtualworkspace.domain.vo;

import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.shared.stage.StageCode;

import java.util.Objects;

public record VirtualWorkspaceId(
        PlayerId playerId,
        StageCode stageCode
) {
    public VirtualWorkspaceId {
        Objects.requireNonNull(playerId, "playerId must not be null");
        Objects.requireNonNull(stageCode, "stageCode must not be null");
    }

    public static VirtualWorkspaceId of(PlayerId playerId, StageCode stageCode) {
        return new VirtualWorkspaceId(playerId, stageCode);
    }
}
