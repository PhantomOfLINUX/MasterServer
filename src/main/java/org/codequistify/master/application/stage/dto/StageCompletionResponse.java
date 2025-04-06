package org.codequistify.master.application.stage.dto;

public record StageCompletionResponse(
        Integer previousExp,
        Integer previousLevel,
        Integer currentExp,
        Integer currentLevel
) {
}
