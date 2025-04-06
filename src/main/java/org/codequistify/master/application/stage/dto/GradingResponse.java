package org.codequistify.master.application.stage.dto;

public record GradingResponse(
        Boolean isCorrect,
        Integer nextIndex,
        Boolean isLast,
        Boolean isComposable
) {
}
