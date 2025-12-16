package org.codequistify.master.domain.lab.vo;

import org.codequistify.master.domain.player.domain.PlayerId;

import java.util.Objects;

public record LabRouteId(
        PlayerId playerId,
        StageCode stageCode
) {
    public LabRouteId {
        Objects.requireNonNull(playerId, "playerId must not be null");
        Objects.requireNonNull(stageCode, "stageCode must not be null");
    }

    public static LabRouteId of(PlayerId playerId, StageCode stageCode) {
        return new LabRouteId(playerId, stageCode);
    }
}
