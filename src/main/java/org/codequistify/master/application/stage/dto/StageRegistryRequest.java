package org.codequistify.master.application.stage.dto;

import org.codequistify.master.core.domain.stage.model.DifficultyLevelType;
import org.codequistify.master.core.domain.stage.model.StageGroupType;

import java.util.List;

public record StageRegistryRequest(
        String title,
        String description,
        StageGroupType groupType,
        DifficultyLevelType difficultyLevel,
        List<QuestionRequest> questions
) {
}
