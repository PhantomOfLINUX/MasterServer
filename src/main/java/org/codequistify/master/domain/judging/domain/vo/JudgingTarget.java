package org.codequistify.master.domain.judging.domain.vo;

import java.util.Objects;

public record JudgingTarget(
        StageCode stageCode,
        LabUserUid uid
) {
    public JudgingTarget {
        Objects.requireNonNull(stageCode, "stageCode must not be null");
        Objects.requireNonNull(uid, "uid must not be null");
    }

    public static JudgingTarget of(StageCode stageCode, LabUserUid uid) {
        return new JudgingTarget(stageCode, uid);
    }
}
