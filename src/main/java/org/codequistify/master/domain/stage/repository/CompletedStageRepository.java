package org.codequistify.master.domain.stage.repository;

import org.codequistify.master.domain.stage.domain.CompletedStage;
import org.codequistify.master.domain.stage.dto.StageInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompletedStageRepository extends JpaRepository<CompletedStage, Long> {
    @Query("SELECT new org.codequistify.master.domain.stage.dto.StageInfo(c.stage.stageImage, '') " +
            "FROM CompletedStage c " +
            "WHERE c.player.id = :playerId AND c.status = org.codequistify.master.domain.stage.domain.CompletedStatus.COMPLETED")
    List<StageInfo> findCompletedStagesByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT new org.codequistify.master.domain.stage.dto.StageInfo(c.stage.stageImage, '') " +
            "FROM CompletedStage c " +
            "WHERE c.player.id = :playerId AND c.status = org.codequistify.master.domain.stage.domain.CompletedStatus.IN_PROGRESS")
    List<StageInfo> findInProgressStagesByPlayerId(@Param("playerId") Long playerId);
}
