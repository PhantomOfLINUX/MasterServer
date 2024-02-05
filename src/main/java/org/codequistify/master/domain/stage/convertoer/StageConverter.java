package org.codequistify.master.domain.stage.convertoer;

import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.springframework.stereotype.Component;

@Component
public class StageConverter {
    public Stage Convert(StageRegistryRequest request) {
        return Stage.builder()
                .title(request.title())
                .description(request.description())
                .difficultyLevel(request.difficultyLevel())
                .questionCount(request.questionCount())
                .questions(request.questions())
                .build();
    }
}
