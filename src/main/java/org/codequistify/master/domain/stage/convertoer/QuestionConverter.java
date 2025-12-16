package org.codequistify.master.domain.stage.convertoer;

import org.codequistify.master.domain.stage.domain.Question;
import org.codequistify.master.domain.stage.dto.QuestionRequest;
import org.codequistify.master.domain.stage.dto.QuestionResponse;
import org.springframework.stereotype.Component;

@Component
public class QuestionConverter {
    public Question convert(QuestionRequest questionRequest) {
        return Question.builder()
                .title(questionRequest.title())
                .description(questionRequest.description())
                .answerType(questionRequest.answerType())
                .correctAnswer(questionRequest.correctAnswer())
                .options(questionRequest.options())
                .build();
    }

    public QuestionResponse convert(Question question) {
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
