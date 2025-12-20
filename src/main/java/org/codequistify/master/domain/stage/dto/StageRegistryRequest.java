package org.codequistify.master.domain.stage.dto;

import java.util.List;
import org.codequistify.master.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.domain.stage.domain.StageGroupType;

public record StageRegistryRequest(
    String title,
    String description,
    StageGroupType groupType,
    DifficultyLevelType difficultyLevel,
    List<QuestionRequest> questions) {}
