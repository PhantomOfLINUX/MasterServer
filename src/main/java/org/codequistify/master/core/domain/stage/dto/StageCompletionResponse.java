package org.codequistify.master.core.domain.stage.dto;

public record StageCompletionResponse(
        Integer previousExp,
        Integer previousLevel,
        Integer currentExp,
        Integer currentLevel
) {
}
