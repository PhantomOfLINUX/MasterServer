package org.codequistify.master.application.stage.dto;

import org.codequistify.master.core.domain.stage.model.StageImageType;

public record StageCodeDTO(
        StageImageType stageCode,
        String accessUri
) {
    public static StageCodeDTO of(StageImageType stageImageType) {
        return new StageCodeDTO(stageImageType, "");
    }
}
