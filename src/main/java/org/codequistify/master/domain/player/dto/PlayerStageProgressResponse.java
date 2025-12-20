package org.codequistify.master.domain.player.dto;

import java.util.List;
import org.codequistify.master.domain.stage.dto.StageCodeDTO;

public record PlayerStageProgressResponse(List<StageCodeDTO> stageCodeDTOS) {}
