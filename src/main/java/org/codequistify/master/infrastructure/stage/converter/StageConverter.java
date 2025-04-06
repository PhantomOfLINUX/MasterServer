package org.codequistify.master.infrastructure.stage.converter;

import org.codequistify.master.core.domain.stage.model.Stage;
import org.codequistify.master.infrastructure.stage.entity.QuestionEntity;
import org.codequistify.master.infrastructure.stage.entity.StageEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StageConverter {

    public static Stage toDomain(StageEntity entity) {
        List<org.codequistify.master.core.domain.stage.model.Question> questions = Optional
                .ofNullable(entity.getQuestions())
                .orElse(List.of())
                .stream()
                .map(QuestionConverter::toDomain)
                .collect(Collectors.toList());

        return Stage.builder()
                    .id(entity.getId())
                    .title(entity.getTitle())
                    .description(entity.getDescription())
                    .stageGroup(entity.getStageGroup())
                    .difficultyLevel(entity.getDifficultyLevel())
                    .questionCount(entity.getQuestionCount())
                    .stageImage(entity.getStageImage())
                    .approved(entity.getApproved())
                    .questions(questions)
                    .build();
    }

    public static StageEntity toEntity(Stage domain) {
        List<QuestionEntity> questionEntities = domain.getQuestions()
                                                      .stream()
                                                      .map(QuestionConverter::toEntity)
                                                      .collect(Collectors.toList());

        StageEntity entity = StageEntity.builder()
                                        .title(domain.getTitle())
                                        .description(domain.getDescription())
                                        .stageGroup(domain.getStageGroup())
                                        .difficultyLevel(domain.getDifficultyLevel())
                                        .questionCount(domain.getQuestionCount())
                                        .stageImage(domain.getStageImage())
                                        .approved(domain.getApproved())
                                        .questions(questionEntities)
                                        .build();

        return entity;
    }
}
