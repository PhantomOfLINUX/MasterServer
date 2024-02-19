package org.codequistify.master.domain.stage.convertoer;

import org.codequistify.master.domain.stage.domain.Question;
import org.codequistify.master.domain.stage.dto.QuestionRequest;
import org.springframework.stereotype.Component;

@Component
public class QuestionConverter {
    public Question convert(QuestionRequest questionRequest) {
        return Question.builder()
                .index(questionRequest.index())
                .title(questionRequest.title())
                .description(questionRequest.description())
                .answerType(questionRequest.answerType())
                .correctAnswer(questionRequest.correctAnswer())
                .options(questionRequest.options())
                .build();
    }
}
