package org.codequistify.master.infrastructure.stage.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.stage.dto.StageResponse;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.model.CompletedStatus;
import org.codequistify.master.core.domain.stage.model.StageSearchCondition;
import org.codequistify.master.domain.stage.domain.QCompletedStage;
import org.codequistify.master.domain.stage.domain.QStage;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StageQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<StageResponse> findStages(StageSearchCondition condition, PageRequest pageRequest, PolId uid) {
        QStage stage = QStage.stage;
        QCompletedStage completed = QCompletedStage.completedStage;

        return queryFactory
                .select(Projections.constructor(StageResponse.class,
                                                stage.id,
                                                Expressions.stringTemplate("cast({0} as string)", stage.stageImage),
                                                stage.title,
                                                stage.description,
                                                stage.stageGroup,
                                                stage.difficultyLevel,
                                                stage.questionCount,
                                                completed.status.coalesce(CompletedStatus.NOT_COMPLETED)))
                .from(stage)
                .leftJoin(stage.completedStages, completed)
                .on(completed.player.uid.eq(uid.getValue()))
                .where(condition.toBooleanBuilder(stage, completed, uid))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();
    }

    public long countStages(StageSearchCondition condition, PolId uid) {
        QStage stage = QStage.stage;
        QCompletedStage completed = QCompletedStage.completedStage;

        return queryFactory
                .from(stage)
                .leftJoin(stage.completedStages, completed)
                .on(completed.player.uid.eq(uid.getValue()))
                .where(condition.toBooleanBuilder(stage, completed, uid))
                .fetchCount();
    }
}
