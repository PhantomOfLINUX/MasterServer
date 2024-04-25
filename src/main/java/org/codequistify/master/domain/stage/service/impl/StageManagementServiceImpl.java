package org.codequistify.master.domain.stage.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.service.LabAssignmentService;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.convertoer.QuestionConverter;
import org.codequistify.master.domain.stage.convertoer.StageConverter;
import org.codequistify.master.domain.stage.domain.*;
import org.codequistify.master.domain.stage.dto.GradingRequest;
import org.codequistify.master.domain.stage.dto.GradingResponse;
import org.codequistify.master.domain.stage.dto.StageActionRequest;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.codequistify.master.domain.stage.repository.CompletedStageRepository;
import org.codequistify.master.domain.stage.repository.QuestionRepository;
import org.codequistify.master.domain.stage.repository.StageRepository;
import org.codequistify.master.domain.stage.service.StageManagementService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.util.SuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StageManagementServiceImpl implements StageManagementService {
    private final StageRepository stageRepository;
    private final QuestionRepository questionRepository;
    private final CompletedStageRepository completedStageRepository;

    private final LabAssignmentService labAssignmentService;

    private final StageConverter stageConverter;
    private final QuestionConverter questionConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(StageManagementServiceImpl.class);

    @Override
    @Transactional
    public void saveStage(StageRegistryRequest request) {
        Stage stage = stageConverter.convert(request);

        stage = stageRepository.save(stage);
    }

    @Override
    @Transactional
    public GradingResponse evaluateAnswer(Player player, GradingRequest request) {
        Question question = questionRepository.findByStageIdAndIndex(request.stageId(), request.questionIndex())
                .orElseThrow(() -> {
                    LOGGER.info("[checkAnswerCorrectness] {}, id: {}, index: {}",
                            ErrorCode.QUESTION_NOT_FOUND.getMessage(), request.stageId(), request.questionIndex());
                    return new BusinessException(ErrorCode.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        boolean isCorrect;
        if (question.getAnswerType().equals(AnswerType.PRACTICAL)) {
            Stage stage = question.getStage();
            isCorrect = this.evaluatePracticalAnswerCorrectness(player, stage, request);
        }
        else {
            isCorrect = this.evaluateStandardAnswerCorrectness(question, request);
        }

        boolean isLast = !questionRepository
                .existsByStageIdAndIndex(request.stageId(), request.questionIndex() + 1);
        int nextIndex = isLast ? -1 : request.questionIndex() + 1;

        return new GradingResponse(
                isCorrect,
                nextIndex,
                isLast
        );
    }

    private boolean evaluateStandardAnswerCorrectness(Question question, GradingRequest request) {
        String correctAnswer = question.getCorrectAnswer();
        return correctAnswer.equalsIgnoreCase(request.answer());
    }

    private boolean evaluatePracticalAnswerCorrectness(Player player, Stage stage, GradingRequest request) {
        StageActionRequest stageActionRequest = new StageActionRequest(
                stage.getStageImage().name(),
                request.questionIndex());

        SuccessResponse response = labAssignmentService
                .sendGradingRequest(stage.getStageImage().name(), player.getUid().toLowerCase(), stageActionRequest)
                .getBody();

        return response.success();
    }

    // compose 메서드
    @Override
    @Transactional
    public SuccessResponse composePShell(Player player, GradingRequest request) {
        Question question = questionRepository.findByStageIdAndIndex(request.stageId(), request.questionIndex())
                .orElseThrow(() -> {
                    LOGGER.info("[checkAnswerCorrectness] {}, id: {}, index: {}",
                            ErrorCode.QUESTION_NOT_FOUND.getMessage(), request.stageId(), request.questionIndex());
                    return new BusinessException(ErrorCode.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
        Stage stage = question.getStage();

        StageActionRequest stageActionRequest = new StageActionRequest(
                stage.getStageImage().name(),
                request.questionIndex());

        SuccessResponse response = labAssignmentService
                .sendComposeRequest(stage.getStageImage().name(), player.getUid().toLowerCase(), stageActionRequest)
                .getBody();

        return response;
    }


    // 스테이지 등록
    @Override
    @Transactional
    public void recordStageComplete(Player player, Long stageId) {
        if (!completedStageRepository.existsByPlayerIdAndStageId(player.getId(), stageId)) {
            Stage stage = stageRepository.findById(stageId)
                    .orElseThrow(() -> {
                        LOGGER.info("[recordStageComplete] {}}, stage: {}",
                                ErrorCode.STAGE_NOT_FOUND.getMessage(), stageId);
                        return new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                    });

            CompletedStage completedStage = CompletedStage.builder()
                    .player(player)
                    .stage(stage)
                    .status(CompletedStatus.COMPLETED).build();
            completedStage.updateQuestionIndex(stage.getQuestionCount());

            completedStage = completedStageRepository.save(completedStage);
            LOGGER.info("[recordStageComplete] player: {}, {} 클리어", player.getUid(), stageId);
        }

        CompletedStage completedStage = completedStageRepository
                .findByPlayerIdAndStageId(player.getId(), stageId)
                .orElseThrow(()->{
                    LOGGER.info("[updateInProgressStage] {}, stage: {}",
                            ErrorCode.STAGE_PROGRESS_NOT_FOUND.getMessage(), stageId);
                    return new BusinessException(ErrorCode.STAGE_PROGRESS_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        completedStage.updateCompleted();
        completedStage = completedStageRepository.save(completedStage);
        LOGGER.info("[recordStageComplete] player: {}, {} 클리어", player.getUid(), stageId);
    }


    @Transactional
    public void recordInProgressStageInit(Player player, GradingRequest request) {
        Stage stage = stageRepository.findById(request.stageId())
                .orElseThrow(() -> {
                    LOGGER.info("[recordStageComplete] {}, stage: {}",
                            ErrorCode.STAGE_NOT_FOUND.getMessage(), request.stageId());
                    return new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        CompletedStage completedStage = CompletedStage.builder()
                .player(player)
                .stage(stage)
                .status(CompletedStatus.IN_PROGRESS).build();

        completedStageRepository.save(completedStage);
        LOGGER.info("[recordInProgressStageInit] 풀이 시작 기록 stage: {}, index: {}",
                request.stageId(), request.questionIndex());
    }

    @Transactional
    public void updateInProgressStage(Player player, GradingRequest request) {
        CompletedStage completedStage = completedStageRepository
                .findByPlayerIdAndStageId(player.getId(), request.stageId())
                .orElseThrow(()->{
                    LOGGER.info("[updateInProgressStage] {}, stage: {}",
                            ErrorCode.STAGE_PROGRESS_NOT_FOUND.getMessage(), request.stageId());
                    return new BusinessException(ErrorCode.STAGE_PROGRESS_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        completedStage.updateQuestionIndex(request.questionIndex());
        completedStageRepository.save(completedStage);
        LOGGER.info("[updateInProgressStage] 진행정도 업데이트 stage: {}, index: {}",
                request.stageId(), request.questionIndex());
    }



}
