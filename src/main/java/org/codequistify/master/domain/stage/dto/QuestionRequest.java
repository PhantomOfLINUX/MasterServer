package org.codequistify.master.domain.stage.dto;

import java.util.List;
import org.codequistify.master.domain.stage.domain.AnswerType;

public record QuestionRequest(
    String title,
    String description,
    AnswerType answerType,
    String correctAnswer,
    List<String> options) {}
