package org.codequistify.master.domain.stage.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.convertoer.StageConverter;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageGroupType;
import org.codequistify.master.domain.stage.dto.StagePageResponse;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.codequistify.master.domain.stage.repository.QuestionRepository;
import org.codequistify.master.domain.stage.repository.StageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StageService {
    private final StageRepository stageRepository;
    private final QuestionRepository questionRepository;

    private final StageConverter stageConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(StageService.class);

    public void saveStage(StageRegistryRequest request) {
        Stage stage = stageConverter.convert(request);

        stage = stageRepository.save(stage);
    }

    public StagePageResponse findStageByGroup(int index, int size, StageGroupType stageGroupType) {
        PageRequest pageRequest = PageRequest.of(index, size);

        if (stageGroupType == null) {
            StagePageResponse response = stageConverter.convert(
                    stageRepository.findAll(pageRequest)
            );
            return response;
        }

        StagePageResponse response = stageConverter.convert(
                stageRepository.findByStageGroup(stageGroupType, pageRequest)
        );
        return response;
    }

}
