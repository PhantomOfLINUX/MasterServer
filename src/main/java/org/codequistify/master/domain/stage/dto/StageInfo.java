package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.StageImageType;

public record StageInfo(
        StageImageType stageCode,
        String accessUri
) {
    public static StageInfo of (StageImageType stageImageType) {
        return new StageInfo(stageImageType, "");
    }
}
