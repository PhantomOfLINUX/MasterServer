package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.domain.stage.domain.StageGroupType;

import java.util.List;

public record StageRegistryRequest(
        Long code,
        String title,
        String description,
        StageGroupType groupType,
        DifficultyLevelType difficultyLevel,
        List<QuestionRequest> questions
) {
}
