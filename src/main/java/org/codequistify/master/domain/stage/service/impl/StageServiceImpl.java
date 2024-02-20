package org.codequistify.master.domain.stage.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.convertoer.QuestionConverter;
import org.codequistify.master.domain.stage.convertoer.StageConverter;
import org.codequistify.master.domain.stage.domain.Question;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.*;
import org.codequistify.master.domain.stage.repository.QuestionRepository;
import org.codequistify.master.domain.stage.repository.StageRepository;
import org.codequistify.master.domain.stage.service.StageService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StageServiceImpl implements StageService {
    private final StageRepository stageRepository;
    private final QuestionRepository questionRepository;

    private final StageConverter stageConverter;
    private final QuestionConverter questionConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(StageServiceImpl.class);

    @Override
    @Transactional
    public void saveStage(StageRegistryRequest request) {
        Stage stage = stageConverter.convert(request);

        stage = stageRepository.save(stage);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public QuestionResponse findQuestion(Long stageId, Integer questionIndex) {
        Question question = questionRepository.findByStageIdAndIndex(stageId, questionIndex)
                .orElseThrow(() -> {
                    LOGGER.info("[findQuestion] {}, id: {}, index: {}", ErrorCode.QUESTION_NOT_FOUND.getMessage(), stageId, questionIndex);
                    return new BusinessException(ErrorCode.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
        QuestionResponse response = questionConverter.convert(question);

        LOGGER.info("[findQuestion] 문항 조회, id: {}, index: {}", stageId, questionIndex);
        return response;
    }

    @Override
    @Transactional
    public GradingResponse checkAnswerCorrectness(GradingRequest request) {
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> {
                    LOGGER.info("");
                    return new BusinessException(ErrorCode.UNKNOWN, HttpStatus.BAD_REQUEST);
                });

        String correctAnswer = question.getCorrectAnswer();
        boolean isCorrect = correctAnswer.equalsIgnoreCase(request.answer());
        boolean isLast = !questionRepository.existsByIndex(request.questionIndex()+1);
        int nextIndex = isLast ? -1 : request.questionIndex()+1;

        return new GradingResponse(
                isCorrect,
                nextIndex,
                isLast
        );
    }

}
