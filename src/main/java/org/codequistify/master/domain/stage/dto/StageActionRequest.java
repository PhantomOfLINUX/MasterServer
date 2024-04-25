package org.codequistify.master.domain.stage.dto;

public record StageActionRequest(
        String stageCode,
        Integer questionIndex
) {
}
