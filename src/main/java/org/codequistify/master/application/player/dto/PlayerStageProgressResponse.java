package org.codequistify.master.application.player.dto;

import org.codequistify.master.core.domain.stage.dto.StageCodeDTO;

import java.util.List;

public record PlayerStageProgressResponse(
        List<StageCodeDTO> stageCodeDTOS
) {
}
