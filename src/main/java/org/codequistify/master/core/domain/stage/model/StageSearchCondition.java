package org.codequistify.master.core.domain.stage.model;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Builder;
import lombok.Getter;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.domain.stage.domain.QCompletedStage;
import org.codequistify.master.domain.stage.domain.QStage;

import java.util.Collections;
import java.util.List;

@Getter
public class StageSearchCondition {

    private final CompletedStatus completedStatus;
    private final List<DifficultyLevelType> difficultyLevels;
    private final String searchText;
    private final List<StageGroupType> stageGroupTypes;

    @Builder
    public StageSearchCondition(
            List<StageGroupType> stageGroupTypes,
            List<DifficultyLevelType> difficultyLevels,
            String searchText,
            CompletedStatus completedStatus
    ) {
        this.stageGroupTypes = stageGroupTypes != null ? List.copyOf(stageGroupTypes) : Collections.emptyList();
        this.difficultyLevels = difficultyLevels != null ? List.copyOf(difficultyLevels) : Collections.emptyList();
        this.searchText = searchText;
        this.completedStatus = completedStatus;
    }

    public BooleanBuilder toBooleanBuilder(QStage qStage, QCompletedStage qCompletedStage, PolId uid) {
        BooleanBuilder builder = new BooleanBuilder();

        if (!stageGroupTypes.isEmpty()) {
            builder.and(qStage.stageGroup.in(stageGroupTypes));
        }

        if (!difficultyLevels.isEmpty()) {
            builder.and(qStage.difficultyLevel.in(difficultyLevels));
        }

        if (searchText != null && !searchText.isBlank()) {
            builder.and(
                    qStage.title.containsIgnoreCase(searchText)
                                .or(qStage.description.containsIgnoreCase(searchText))
                                .or(Expressions.stringTemplate("cast({0} as string)", qStage.stageImage)
                                               .containsIgnoreCase(searchText))
            );
        }

        if (completedStatus != null) {
            builder.and(
                    completedStatus == CompletedStatus.NOT_COMPLETED
                            ? qCompletedStage.id.isNull()
                            : qCompletedStage.status.eq(completedStatus)
                                                    .and(qCompletedStage.player.uid.eq(uid.getValue()))
            );
        }

        builder.and(qStage.approved.eq(true));
        return builder;
    }
}
