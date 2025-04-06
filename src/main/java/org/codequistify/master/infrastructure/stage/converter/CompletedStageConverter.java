package org.codequistify.master.infrastructure.stage.converter;

import org.codequistify.master.core.domain.stage.model.CompletedStage;
import org.codequistify.master.infrastructure.player.converter.PlayerConverter;
import org.codequistify.master.infrastructure.stage.entity.CompletedStageEntity;

public class CompletedStageConverter {

    public static CompletedStage toDomain(CompletedStageEntity entity) {
        return CompletedStage.builder()
                             .id(entity.getId())
                             .player(PlayerConverter.toDomain(entity.getPlayer()))
                             .stage(StageConverter.toDomain(entity.getStage()))
                             .status(entity.getStatus())
                             .questionIndex(entity.getQuestionIndex())
                             .createdAt(entity.getCreatedDate())
                             .updatedAt(entity.getCreatedDate())
                             .build();
    }

    public static CompletedStageEntity toEntity(CompletedStage domain) {
        return CompletedStageEntity.builder()
                                   .player(PlayerConverter.toEntity(domain.getPlayer()))
                                   .stage(StageConverter.toEntity(domain.getStage()))
                                   .status(domain.getStatus())
                                   .build();
    }
}
