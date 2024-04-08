package org.codequistify.master.domain.stage.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.codequistify.master.domain.stage.domain.CompletedStatus;
import org.codequistify.master.domain.stage.domain.DifficultyLevelType;
import org.codequistify.master.domain.stage.domain.StageGroupType;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchCriteria {
    @Positive(message = "4104")
    private int page_index = 1;

    @Positive(message = "4104")
    private int page_size = 10;

    private List<StageGroupType> stageGroupTypes = new ArrayList<>();

    private List<DifficultyLevelType> difficultyLevels = new ArrayList<>();

    private CompletedStatus completed;

    private String searchText;
}
