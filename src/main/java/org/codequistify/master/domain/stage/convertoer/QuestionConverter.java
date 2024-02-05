package org.codequistify.master.domain.stage.convertoer;

import org.codequistify.master.domain.stage.domain.Question;
import org.codequistify.master.domain.stage.dto.QuestionDTO;
import org.springframework.stereotype.Component;

@Component
public class QuestionConverter {
    public Question convert(QuestionDTO questionDTO) {
        return Question.builder()
                .index(questionDTO.index())
                .title(questionDTO.title())
                .description(questionDTO.description())
                .answerType(questionDTO.answerType())
                .correctAnswer(questionDTO.correctAnswer())
                .options(questionDTO.options())
                .build();
    }
}
