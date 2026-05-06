package org.codequistify.master.domain.stage.dto;

public record StageCompletionResponse(
        Integer previousExp,
        Integer previousLevel,
        Integer currentExp,
        Integer currentLevel
) {
}
