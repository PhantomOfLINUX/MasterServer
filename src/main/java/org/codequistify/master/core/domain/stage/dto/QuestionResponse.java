package org.codequistify.master.core.domain.stage.dto;

import org.codequistify.master.core.domain.stage.domain.AnswerType;

import java.util.List;

public record QuestionResponse(
        Long questionId,
        Integer index,
        String title,
        String description,
        AnswerType answerType,
        List<String> options
) {
}
