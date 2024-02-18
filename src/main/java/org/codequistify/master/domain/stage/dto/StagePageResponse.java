package org.codequistify.master.domain.stage.dto;

import java.util.List;

public record StagePageResponse(
        List<StageResponse> stages,
        PageParameters pageParameters
) {
}
