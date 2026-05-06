package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.StageImageType;

public record StageCodeDTO(
        StageImageType stageCode,
        String accessUri
) {
    public static StageCodeDTO of (StageImageType stageImageType) {
        return new StageCodeDTO(stageImageType, "");
    }
}
