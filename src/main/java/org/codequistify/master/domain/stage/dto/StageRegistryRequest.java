package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.domain.stage.domain.Question;

import java.util.List;

public record StageRegistryRequest(
        String title,
        String description,
        DifficultyLevelType difficultyLevel,
        Integer questionCount,
        List<Question> questions
) {
}
