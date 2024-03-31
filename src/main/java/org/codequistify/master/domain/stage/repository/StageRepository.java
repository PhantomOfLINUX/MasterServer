package org.codequistify.master.domain.stage.repository;

import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageGroupType;
import org.codequistify.master.domain.stage.dto.StageResponseTEMP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {
    Page<Stage> findByStageGroup(StageGroupType stageGroup, Pageable pageable);

    @Query("SELECT new org.codequistify.master.domain.stage.dto." +
            "StageResponseTEMP(s.id, s.title, s.description, s.stageGroup, s.difficultyLevel, s.questionCount, " +
            "CASE WHEN c IS NULL THEN 'NOT_COMPLETED' ELSE c.status END) " +
            "FROM Stage s " +
            "LEFT JOIN CompletedStage c ON s.id = c.stage.id AND c.player.id = :playerId")
    List<StageResponseTEMP> findAllByPlayerIdWithCompleted(@Param("playerId") Long playerId);



}
