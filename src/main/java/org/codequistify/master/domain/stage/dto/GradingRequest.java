package org.codequistify.master.domain.stage.dto;

public record GradingRequest(
        Long stageId,
        Integer questionIndex,
        String answer
) {
}
