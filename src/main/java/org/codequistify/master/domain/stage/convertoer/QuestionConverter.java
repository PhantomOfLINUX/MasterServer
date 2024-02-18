package org.codequistify.master.domain.stage.convertoer;

import org.codequistify.master.domain.stage.domain.Question;
import org.codequistify.master.domain.stage.dto.QuestionResponse;
import org.springframework.stereotype.Component;

@Component
public class QuestionConverter {
    public Question convert(QuestionResponse questionResponse) {
        return Question.builder()
                .index(questionResponse.index())
                .title(questionResponse.title())
                .description(questionResponse.description())
                .answerType(questionResponse.answerType())
                .correctAnswer(questionResponse.correctAnswer())
                .options(questionResponse.options())
                .build();
    }
}
