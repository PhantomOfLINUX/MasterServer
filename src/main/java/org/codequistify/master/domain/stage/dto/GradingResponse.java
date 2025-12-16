package org.codequistify.master.domain.stage.dto;

public record GradingResponse(
        Boolean isCorrect,
        Integer nextIndex,
        Boolean isLast,
        Boolean isComposable
) {
}
