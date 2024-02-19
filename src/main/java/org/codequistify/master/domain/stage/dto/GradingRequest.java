package org.codequistify.master.domain.stage.dto;

public record GradingRequest(
        String questionId,
        Integer questionIndex,
        String answer
) {
}
