package org.codequistify.master.core.domain.stage.dto;

import java.util.List;

public record StagePageResponse(
        List<StageResponse> stages,
        PageParameters pageParameters
) {
    public static StagePageResponse of(List<StageResponse> stages, PageParameters pageParameters) {
        return new StagePageResponse(stages, pageParameters);
    }
}
