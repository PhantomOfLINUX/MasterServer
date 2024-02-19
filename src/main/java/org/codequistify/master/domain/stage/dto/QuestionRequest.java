package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.AnswerType;

import java.util.List;

public record QuestionRequest(
        String title,
        String description,
        AnswerType answerType,
        String correctAnswer,
        List<String> options
) {
}
