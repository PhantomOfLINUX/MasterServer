package org.codequistify.master.core.domain.stage.dto;

import org.codequistify.master.core.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.core.domain.stage.domain.StageGroupType;

import java.util.List;

public record StageRegistryRequest(
        String title,
        String description,
        StageGroupType groupType,
        DifficultyLevelType difficultyLevel,
        List<QuestionRequest> questions
) {
}
