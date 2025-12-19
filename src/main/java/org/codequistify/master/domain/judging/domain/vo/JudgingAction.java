package org.codequistify.master.domain.judging.domain.vo;

import java.util.Objects;

public record JudgingAction(
        StageCode stageCode,
        Integer questionIndex
) {
    public JudgingAction {
        Objects.requireNonNull(stageCode, "stageCode must not be null");
        Objects.requireNonNull(questionIndex, "questionIndex must not be null");
    }

    public static JudgingAction of(StageCode stageCode, Integer questionIndex) {
        return new JudgingAction(stageCode, questionIndex);
    }
}
