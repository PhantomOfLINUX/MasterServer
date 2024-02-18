package org.codequistify.master.domain.stage.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.convertoer.StageConverter;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.SearchCriteria;
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
public class StageServiceImpl {
    private final StageRepository stageRepository;
    private final QuestionRepository questionRepository;

    private final StageConverter stageConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(StageServiceImpl.class);

    public void saveStage(StageRegistryRequest request) {
        Stage stage = stageConverter.convert(request);

        stage = stageRepository.save(stage);
    }

    public StagePageResponse findStageByGroup(SearchCriteria searchCriteria) {
        PageRequest pageRequest = PageRequest.of(searchCriteria.page_index()-1, searchCriteria.page_size());

        if (searchCriteria.stageGroupType() == null) {
            StagePageResponse response = stageConverter.convert(
                    stageRepository.findAll(pageRequest)
            );
            return response;
        }

        StagePageResponse response = stageConverter.convert(
                stageRepository.findByStageGroup(searchCriteria.stageGroupType(), pageRequest)
        );
        return response;
    }

}
