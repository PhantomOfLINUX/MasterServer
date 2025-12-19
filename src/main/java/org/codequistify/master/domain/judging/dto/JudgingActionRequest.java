package org.codequistify.master.domain.judging.dto;

import org.codequistify.master.domain.judging.domain.vo.JudgingAction;

import java.util.Objects;

public record JudgingActionRequest(
        String stageCode,
        Integer questionIndex
) {
    public JudgingActionRequest {
        Objects.requireNonNull(stageCode, "stageCode must not be null");
        Objects.requireNonNull(questionIndex, "questionIndex must not be null");
    }

    public static JudgingActionRequest from(JudgingAction action) {
        return new JudgingActionRequest(
                action.stageCode().lowercase(),
                action.questionIndex()
        );
    }
}
