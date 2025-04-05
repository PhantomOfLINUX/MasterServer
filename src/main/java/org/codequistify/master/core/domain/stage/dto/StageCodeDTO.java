package org.codequistify.master.core.domain.stage.dto;

import org.codequistify.master.core.domain.stage.domain.StageImageType;

public record StageCodeDTO(
        StageImageType stageCode,
        String accessUri
) {
    public static StageCodeDTO of (StageImageType stageImageType) {
        return new StageCodeDTO(stageImageType, "");
    }
}
