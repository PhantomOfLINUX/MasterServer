package org.codequistify.master.domain.judging.dto;

import org.codequistify.master.domain.judging.domain.vo.JudgingAction;
import org.codequistify.master.domain.judging.domain.vo.JudgingTarget;

import java.util.Objects;

public record JudgingActionRequest(
        String stageCode,
        Integer questionIndex
) {
    public JudgingActionRequest {
        Objects.requireNonNull(stageCode, "stageCode must not be null");
        Objects.requireNonNull(questionIndex, "questionIndex must not be null");
    }

    public static JudgingActionRequest from(JudgingTarget target, JudgingAction action) {
        return new JudgingActionRequest(
                target.stageCode().lowercase(),
                action.questionIndex()
        );
    }
}
