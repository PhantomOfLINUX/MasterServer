package org.codequistify.master.application.stage.dto;

import org.codequistify.master.core.domain.stage.model.CompletedStatus;
import org.codequistify.master.core.domain.stage.model.DifficultyLevelType;
import org.codequistify.master.core.domain.stage.model.StageGroupType;

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
