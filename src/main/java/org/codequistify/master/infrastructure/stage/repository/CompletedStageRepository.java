package org.codequistify.master.infrastructure.stage.repository;

import org.codequistify.master.application.stage.dto.HeatMapDataPoint;
import org.codequistify.master.application.stage.dto.StageCodeDTO;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.infrastructure.stage.entity.CompletedStageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompletedStageRepository extends JpaRepository<CompletedStageEntity, Long> {

    @Query("""
            SELECT new org.codequistify.master.application.stage.dto.StageCodeDTO(c.stage.stageImage, '')
            FROM CompletedStageEntity c
            WHERE c.player.uid = :playerId AND c.status = org.codequistify.master.core.domain.stage.model.CompletedStatus.COMPLETED
            """)
    List<StageCodeDTO> findCompletedStagesByPlayerId(@Param("playerId") PolId playerId);

    @Query("""
            SELECT new org.codequistify.master.application.stage.dto.StageCodeDTO(c.stage.stageImage, '')
            FROM CompletedStageEntity c
            WHERE c.player.uid = :playerId AND c.status = org.codequistify.master.core.domain.stage.model.CompletedStatus.IN_PROGRESS
            """)
    List<StageCodeDTO> findInProgressStagesByPlayerId(@Param("playerId") PolId playerId);

    @Query("""
            SELECT new org.codequistify.master.application.stage.dto.HeatMapDataPoint(DATE(c.modifiedDate), COUNT(*))
            FROM CompletedStageEntity c
            WHERE c.player.uid = :playerId
            GROUP BY DATE(c.modifiedDate)
            """)
    List<HeatMapDataPoint> countDataByModifiedDate(@Param("playerId") PolId playerId);

    Optional<CompletedStageEntity> findByPlayerIdAndStageId(PolId playerId, Long stageId);

    boolean existsByPlayerIdAndStageId(PolId playerId, Long stageId);
}
