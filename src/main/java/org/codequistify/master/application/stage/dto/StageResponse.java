package org.codequistify.master.application.stage.dto;

import org.codequistify.master.core.domain.stage.model.DifficultyLevelType;
import org.codequistify.master.core.domain.stage.model.Stage;
import org.codequistify.master.core.domain.stage.model.StageGroupType;

public record StageResponse(
        Long id,
        String stageCode,
        String title,
        String description,
        StageGroupType stageGroupType,
        DifficultyLevelType difficultyLevelType,
        Integer questionCount) {

    public static StageResponse from(Stage stage) {
        return new StageResponse(
                stage.getId(),
                stage.getStageImage().name(),
                stage.getTitle(),
                stage.getDescription(),
                stage.getStageGroup(),
                stage.getDifficultyLevel(),
                stage.getQuestionCount()
        );
    }
}