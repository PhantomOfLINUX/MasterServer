package org.codequistify.master.core.domain.stage.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.player.service.PlayerProfileService;
import org.codequistify.master.core.domain.lab.service.LabAssignmentService;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.stage.convertoer.QuestionConverter;
import org.codequistify.master.core.domain.stage.convertoer.StageConverter;
import org.codequistify.master.core.domain.stage.domain.*;
import org.codequistify.master.core.domain.stage.dto.*;
import org.codequistify.master.core.domain.stage.repository.CompletedStageRepository;
import org.codequistify.master.core.domain.stage.repository.QuestionRepository;
import org.codequistify.master.core.domain.stage.repository.StageRepository;
import org.codequistify.master.core.domain.stage.service.StageManagementService;
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
    private final PlayerProfileService playerProfileService;

    private final StageConverter stageConverter;
    private final QuestionConverter questionConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(StageManagementServiceImpl.class);
    private final static int LEVEL_STEP_SIZE = 500;

    @Override
    @Transactional
    public void saveStage(StageRegistryRequest request) {
        Stage stage = stageConverter.convert(request);
        stage.updateStageImage(StageImageType.S0000);

        stage = stageRepository.save(stage);
    }

    @Override
    @Transactional
    public GradingResponse evaluateAnswer(Player player, GradingRequest request) {
        Question question = questionRepository.findByStageIdAndIndex(request.stageId(), request.questionIndex())
                                              .orElseThrow(() -> {
                    LOGGER.info("[checkAnswerCorrectness] {}, id: {}, index: {}",
                            ErrorCode.QUESTION_NOT_FOUND.getMessage(), request.stageId(), request.questionIndex());
                                                  return new ApplicationException(ErrorCode.QUESTION_NOT_FOUND,
                                                                                  HttpStatus.NOT_FOUND);
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

        Boolean isComposable = questionRepository
                        .isComposableForStageAndIndex(request.stageId(), request.questionIndex());
        if (isComposable == null) {
            isComposable = false;
        }

        return new GradingResponse(
                isCorrect,
                nextIndex,
                isLast,
                isComposable);
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
                    return new ApplicationException(ErrorCode.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND);
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


    // 스테이지 클리어 기록
    @Override
    @Transactional
    public StageCompletionResponse recordStageComplete(Player player, Long stageId) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    LOGGER.info("[recordStageComplete] {}}, stage: {}",
                            ErrorCode.STAGE_NOT_FOUND.getMessage(), stageId);
                    return new ApplicationException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        CompletedStage completedStage;
        if (!completedStageRepository.existsByPlayerIdAndStageId(player.getId(), stageId)) {
            completedStage = CompletedStage.builder()
                                           .player(player)
                                           .stage(stage)
                                           .status(CompletedStatus.COMPLETED).build();
        }
        else {
            completedStage = completedStageRepository
                    .findByPlayerIdAndStageId(player.getId(), stageId)
                    .orElseThrow(() -> {
                        LOGGER.info("[updateInProgressStage] {}, stage: {}",
                                ErrorCode.STAGE_PROGRESS_NOT_FOUND.getMessage(), stageId);
                        return new ApplicationException(ErrorCode.STAGE_PROGRESS_NOT_FOUND, HttpStatus.NOT_FOUND);
                    });
            completedStage.updateCompleted();
        }
        completedStage.updateQuestionIndex(stage.getQuestionCount());
        completedStage = completedStageRepository.save(completedStage);

        int updatedExp = playerProfileService.increaseExp(player, stage.getDifficultyLevel().getExp());

        StageCompletionResponse response = new StageCompletionResponse(
                player.getExp(),
                player.getExp() / LEVEL_STEP_SIZE,
                updatedExp,
                updatedExp / LEVEL_STEP_SIZE
        );

        LOGGER.info("[recordStageComplete] player: {}, {} 클리어", player.getUid(), stageId);
        return response;
    }


    @Transactional
    public void recordInProgressStageInit(Player player, GradingRequest request) {
        if (completedStageRepository
                .existsByPlayerIdAndStageId(player.getId(), request.stageId())) {
            return;
        }

        Stage stage = stageRepository.findById(request.stageId())
                .orElseThrow(() -> {
                    LOGGER.info("[recordStageComplete] {}, stage: {}",
                            ErrorCode.STAGE_NOT_FOUND.getMessage(), request.stageId());
                    return new ApplicationException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
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
                    return new ApplicationException(ErrorCode.STAGE_PROGRESS_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        completedStage.updateQuestionIndex(request.questionIndex());
        completedStageRepository.save(completedStage);
        LOGGER.info("[updateInProgressStage] 진행정도 업데이트 stage: {}, index: {}",
                request.stageId(), request.questionIndex());
    }



}
