package org.codequistify.master.domain.player.dto;

import org.codequistify.master.domain.stage.dto.StageInfo;

import java.util.List;

public record PlayerStageProgressResponse(
        List<StageInfo> stageInfos
) {
}
