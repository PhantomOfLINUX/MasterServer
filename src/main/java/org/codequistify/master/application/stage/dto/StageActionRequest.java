package org.codequistify.master.application.stage.dto;

public record StageActionRequest(
        String stageCode,
        Integer questionIndex
) {
}
