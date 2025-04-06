package org.codequistify.master.core.domain.stage.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Getter
@ToString
@Builder(toBuilder = true)
public class Question {
    private final AnswerType answerType;
    private final boolean composable;
    private final String correctAnswer;
    private final String description;
    private final Long id;
    private final Integer index;
    private final List<String> options;
    private final Stage stage;
    private final String title;

    public Question(Long id,
                    Integer index,
                    String title,
                    String description,
                    AnswerType answerType,
                    String correctAnswer,
                    boolean composable,
                    List<String> options,
                    Stage stage) {
        this.id = id;
        this.index = index;
        this.title = title;
        this.description = description;
        this.answerType = answerType;
        this.correctAnswer = correctAnswer;
        this.composable = composable;
        this.options = Optional.ofNullable(options).map(List::copyOf).orElse(List.of());
        this.stage = stage;
    }

    public Question withStage(Stage stage) {
        return this.toBuilder().stage(stage).build();
    }
}
