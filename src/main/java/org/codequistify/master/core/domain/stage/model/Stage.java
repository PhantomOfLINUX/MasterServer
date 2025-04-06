package org.codequistify.master.core.domain.stage.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Getter
@ToString
@Builder(toBuilder = true)
public class Stage {

    private final Boolean approved;
    private final String description;
    private final DifficultyLevelType difficultyLevel;
    private final Long id;
    private final Integer questionCount;
    private final List<Question> questions;
    private final StageGroupType stageGroup;
    private final StageImageType stageImage;
    private final String title;

    public Stage(Long id,
                 String title,
                 String description,
                 StageGroupType stageGroup,
                 DifficultyLevelType difficultyLevel,
                 Integer questionCount,
                 StageImageType stageImage,
                 Boolean approved,
                 List<Question> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stageGroup = stageGroup;
        this.difficultyLevel = difficultyLevel;
        this.questionCount = questionCount;
        this.stageImage = stageImage;
        this.approved = approved;
        this.questions = Optional.ofNullable(questions)
                                 .map(List::copyOf)
                                 .orElse(List.of());
    }

    public Stage withQuestionCount(int questionCount) {
        return this.toBuilder().questionCount(questionCount).build();
    }

    public Stage withStageImage(StageImageType imageType) {
        return this.toBuilder().stageImage(imageType).build();
    }

    public Stage withQuestions(List<Question> questions) {
        return this.toBuilder().questions(List.copyOf(questions)).build();
    }
}
