package org.codequistify.master.infrastructure.stage.repository;

import org.codequistify.master.core.domain.stage.model.StageGroupType;
import org.codequistify.master.infrastructure.stage.entity.StageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageRepository extends JpaRepository<StageEntity, Long> {
    Page<StageEntity> findByStageGroup(StageGroupType stageGroup, Pageable pageable);

    /*@Query("SELECT new org.codequistify.master.domain.stageEntity.dto." +
            "StageResponseTEMP(s.id, s.title, s.description, s.stageGroup, s.difficultyLevel, s.questionCount, " +
            "CASE WHEN c IS NULL THEN 'NOT_COMPLETED' ELSE c.status END) " +
            "FROM org.codequistify.master.core.domain.stageEntity.model.StageEntity s " +
            "LEFT JOIN CompletedStageEntity c ON s.id = c.stageEntity.id AND c.player.id = :playerId")
    List<StageResponseTEMP> findAllByPlayerIdWithCompleted(@Param("playerId") Long playerId);*/


}
