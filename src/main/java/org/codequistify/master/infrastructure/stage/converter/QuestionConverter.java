package org.codequistify.master.infrastructure.stage.converter;

import org.codequistify.master.core.domain.stage.model.Question;
import org.codequistify.master.infrastructure.stage.entity.QuestionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionConverter {

    public static Question toDomain(QuestionEntity entity) {
        return Question.builder()
                       .id(entity.getId())
                       .index(entity.getIndex())
                       .title(entity.getTitle())
                       .description(entity.getDescription())
                       .answerType(entity.getAnswerType())
                       .correctAnswer(entity.getCorrectAnswer())
                       .composable(entity.isComposable())
                       .options(new ArrayList<>(Optional.ofNullable(entity.getOptions()).orElse(List.of())))
                       .stage(StageConverter.toDomain(entity.getStage()))
                       .build();
    }

    public static QuestionEntity toEntity(Question domain) {
        return QuestionEntity.builder()
                             .index(domain.getIndex())
                             .title(domain.getTitle())
                             .description(domain.getDescription())
                             .answerType(domain.getAnswerType())
                             .correctAnswer(domain.getCorrectAnswer())
                             .composable(domain.isComposable())
                             .options(new ArrayList<>(domain.getOptions()))
                             .build();
    }
}
