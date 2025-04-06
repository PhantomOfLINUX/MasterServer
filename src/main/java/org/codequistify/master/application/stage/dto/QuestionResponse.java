package org.codequistify.master.application.stage.dto;

import org.codequistify.master.core.domain.stage.model.AnswerType;
import org.codequistify.master.core.domain.stage.model.Question;

import java.util.List;

public record QuestionResponse(
        Long questionId,
        Integer index,
        String title,
        String description,
        AnswerType answerType,
        List<String> options
) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getIndex(),
                question.getTitle(),
                question.getDescription(),
                question.getAnswerType(),
                question.getOptions()
        );
    }
}
