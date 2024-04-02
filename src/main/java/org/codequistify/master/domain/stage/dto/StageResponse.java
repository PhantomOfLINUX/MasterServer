package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.CompletedStatus;
import org.codequistify.master.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.domain.stage.domain.StageGroupType;

public record StageResponse(
        Long id,
        String title,
        String description,
        StageGroupType stageGroupType,
        DifficultyLevelType difficultyLevelType,
        Integer questionCount,
        CompletedStatus completedStatus) {
}
