package org.codequistify.master.domain.stage.convertoer;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StageConverter {
    private final QuestionConverter questionConverter;
    public Stage convert(StageRegistryRequest request) {

        return Stage.builder()
                .title(request.title())
                .description(request.description())
                .difficultyLevel(request.difficultyLevel())
                .questionCount(request.questions().size())
                .questions(
                        request.questions().stream()
                                .map(questionConverter::convert)
                                .collect(Collectors.toList()))
                .build();
    }
}
