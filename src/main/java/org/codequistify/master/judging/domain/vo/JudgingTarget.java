package org.codequistify.master.judging.domain.vo;

import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.shared.stage.StageCode;

import java.util.Objects;

public record JudgingTarget(
        PlayerId playerId,
        StageCode stageCode
) {
    public JudgingTarget {
        Objects.requireNonNull(playerId, "playerId must not be null");
        Objects.requireNonNull(stageCode, "stageCode must not be null");
    }

    public static JudgingTarget of(PlayerId playerId, StageCode stageCode) {
        return new JudgingTarget(playerId, stageCode);
    }
}
