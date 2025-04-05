package org.codequistify.master.core.domain.stage.dto;

public record GradingResponse(
        Boolean isCorrect,
        Integer nextIndex,
        Boolean isLast,
        Boolean isComposable
) {
}
