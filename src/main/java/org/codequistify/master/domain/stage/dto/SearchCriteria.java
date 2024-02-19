package org.codequistify.master.domain.stage.dto;

import jakarta.validation.constraints.Positive;
import org.codequistify.master.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.domain.stage.domain.StageGroupType;

public record SearchCriteria(
        @Positive(message = "4104") int page_index,
        @Positive(message = "4104") int page_size,
        StageGroupType stageGroupType,
        DifficultyLevelType level,
        Boolean isSolved
) {
}
