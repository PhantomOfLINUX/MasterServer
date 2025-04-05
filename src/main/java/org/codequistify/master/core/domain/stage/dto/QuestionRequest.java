package org.codequistify.master.core.domain.stage.dto;

import org.codequistify.master.core.domain.stage.domain.AnswerType;

import java.util.List;

public record QuestionRequest(
        String title,
        String description,
        AnswerType answerType,
        String correctAnswer,
        List<String> options
) {
}
