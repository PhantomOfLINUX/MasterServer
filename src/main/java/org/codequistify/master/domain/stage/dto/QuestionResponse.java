package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.AnswerType;

import java.util.List;

public record QuestionResponse(
        Integer index,
        String title,
        String description,
        AnswerType answerType,
        String correctAnswer,
        List<String> options
) {
}
