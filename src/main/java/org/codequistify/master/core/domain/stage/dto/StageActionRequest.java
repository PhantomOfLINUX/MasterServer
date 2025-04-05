package org.codequistify.master.core.domain.stage.dto;

public record StageActionRequest(
        String stageCode,
        Integer questionIndex
) {
}
