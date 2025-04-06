package org.codequistify.master.application.stage.dto;

import org.codequistify.master.core.domain.stage.model.AnswerType;

import java.util.List;

public record QuestionRequest(
        String title,
        String description,
        AnswerType answerType,
        String correctAnswer,
        List<String> options
) {
}
