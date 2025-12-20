package org.codequistify.master.judging.domain.vo;

import java.util.Objects;

public record JudgingAction(
        Integer questionIndex
) {
    public JudgingAction {
        Objects.requireNonNull(questionIndex, "questionIndex must not be null");
    }

    public static JudgingAction of(Integer questionIndex) {
        return new JudgingAction(questionIndex);
    }
}
