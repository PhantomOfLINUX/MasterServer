package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.domain.stage.domain.StageGroupType;

public record SearchCriteria(
        int page_index,
        int page_size,
        StageGroupType stageGroupType,
        DifficultyLevelType level,
        Boolean isSolved
) {
}
