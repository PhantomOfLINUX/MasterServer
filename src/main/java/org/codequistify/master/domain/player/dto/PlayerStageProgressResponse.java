package org.codequistify.master.domain.player.dto;

import org.codequistify.master.domain.stage.dto.StageCodeDTO;

import java.util.List;

public record PlayerStageProgressResponse(
        List<StageCodeDTO> stageCodeDTOS
) {
}
