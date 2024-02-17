package org.codequistify.master.domain.stage.convertoer;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.PageParameters;
import org.codequistify.master.domain.stage.dto.StagePageResponse;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.codequistify.master.domain.stage.dto.StageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StageConverter {
    private final QuestionConverter questionConverter;

    public Stage convert(StageRegistryRequest request) {
        return Stage.builder()
                .title(request.title())
                .description(request.description())
                .stageGroup(request.groupType())
                .difficultyLevel(request.difficultyLevel())
                .questionCount(request.questions().size())
                .questions(
                        request.questions().stream()
                                .map(questionConverter::convert)
                                .collect(Collectors.toList()))
                .build();
    }

    public StageResponse convert(Stage stage) {
        return new StageResponse(
                stage.getId(),
                stage.getTitle(),
                stage.getDescription(),
                stage.getStageGroup(),
                stage.getDifficultyLevel(),
                stage.getQuestionCount());
    }

    public List<StageResponse> convert(List<Stage> stages) {
        return stages.stream()
                .map(this::convert).collect(Collectors.toList());
    }

    public StagePageResponse convert(Page<Stage> stages) {
        PageParameters pageParameters = new PageParameters(
                stages.getTotalPages(),
                stages.getSize(),
                stages.getNumber()+1,
                stages.getNumberOfElements()
        );

        List<StageResponse> response = this.convert(stages.getContent());

        return new StagePageResponse(response, pageParameters);
    }
}
