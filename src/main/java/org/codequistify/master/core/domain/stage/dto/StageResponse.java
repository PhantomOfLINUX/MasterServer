package org.codequistify.master.core.domain.stage.dto;

import org.codequistify.master.core.domain.stage.domain.CompletedStatus;
import org.codequistify.master.core.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.core.domain.stage.domain.StageGroupType;

public record StageResponse(
        Long id,
        String stageCode,
        String title,
        String description,
        StageGroupType stageGroupType,
        DifficultyLevelType difficultyLevelType,
        Integer questionCount,
        CompletedStatus completedStatus) {
}
