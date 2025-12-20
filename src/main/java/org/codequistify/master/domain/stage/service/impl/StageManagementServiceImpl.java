package org.codequistify.master.domain.stage.service.impl;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.player.service.PlayerProfileService;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.codequistify.master.domain.stage.convertoer.QuestionConverter;
import org.codequistify.master.domain.stage.convertoer.StageConverter;
import org.codequistify.master.domain.stage.domain.*;
import org.codequistify.master.domain.stage.dto.*;
import org.codequistify.master.domain.stage.repository.CompletedStageRepository;
import org.codequistify.master.domain.stage.repository.QuestionRepository;
import org.codequistify.master.domain.stage.repository.StageRepository;
import org.codequistify.master.domain.stage.service.StageManagementService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.util.SuccessResponse;
import org.codequistify.master.judging.application.JudgingService;
import org.codequistify.master.judging.domain.vo.JudgingAction;
import org.codequistify.master.judging.domain.vo.JudgingTarget;
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

  private final JudgingService judgingService;
  private final PlayerProfileService playerProfileService;

  private final StageConverter stageConverter;
  private final QuestionConverter questionConverter;
  private final Logger LOGGER = LoggerFactory.getLogger(StageManagementServiceImpl.class);
  private static final int LEVEL_STEP_SIZE = 500;

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
    Stage stage = loadStageByCode(request.stageCode());
    Long stageId = stage.getId();
    Question question = questionRepository
        .findByStageIdAndIndex(stageId, request.questionIndex())
        .orElseThrow(() -> {
          LOGGER.info(
              "[checkAnswerCorrectness] {}, stageCode: {}, index: {}",
              ErrorCode.QUESTION_NOT_FOUND.getMessage(),
              request.stageCode(),
              request.questionIndex());
          return new BusinessException(ErrorCode.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND);
        });

    boolean isCorrect;
    if (question.getAnswerType().equals(AnswerType.PRACTICAL)) {
      isCorrect = this.evaluatePracticalAnswerCorrectness(stage, request);
    } else {
      isCorrect = this.evaluateStandardAnswerCorrectness(question, request);
    }

    boolean isLast =
        !questionRepository.existsByStageIdAndIndex(stageId, request.questionIndex() + 1);
    int nextIndex = isLast ? -1 : request.questionIndex() + 1;

    Boolean isComposable =
        questionRepository.isComposableForStageAndIndex(stageId, request.questionIndex());
    if (isComposable == null) {
      isComposable = false;
    }

    return new GradingResponse(isCorrect, nextIndex, isLast, isComposable);
  }

  private boolean evaluateStandardAnswerCorrectness(Question question, GradingRequest request) {
    String correctAnswer = question.getCorrectAnswer();
    return correctAnswer.equalsIgnoreCase(request.answer());
  }

  private boolean evaluatePracticalAnswerCorrectness(Stage stage, GradingRequest request) {
    StageCode stageCode = StageCode.from(stage.getStageImage().name());
    JudgingTarget target = JudgingTarget.of(PlayerId.of(request.playerId()), stageCode);
    JudgingAction action = JudgingAction.of(request.questionIndex());

    SuccessResponse response = judgingService.requestGrading(target, action).getBody();

    return response.success();
  }

  // compose 메서드
  @Override
  @Transactional
  public SuccessResponse composeVirtualWorkspace(Player player, GradingRequest request) {
    Stage stage = loadStageByCode(request.stageCode());
    Long stageId = stage.getId();
    Question question = questionRepository
        .findByStageIdAndIndex(stageId, request.questionIndex())
        .orElseThrow(() -> {
          LOGGER.info(
              "[checkAnswerCorrectness] {}, stageCode: {}, index: {}",
              ErrorCode.QUESTION_NOT_FOUND.getMessage(),
              request.stageCode(),
              request.questionIndex());
          return new BusinessException(ErrorCode.QUESTION_NOT_FOUND, HttpStatus.NOT_FOUND);
        });

    StageCode stageCode = StageCode.from(stage.getStageImage().name());
    JudgingTarget target = JudgingTarget.of(PlayerId.of(request.playerId()), stageCode);
    JudgingAction action = JudgingAction.of(request.questionIndex());

    SuccessResponse response = judgingService.requestCompose(target, action).getBody();

    return response;
  }

  // 스테이지 클리어 기록
  @Override
  @Transactional
  public StageCompletionResponse recordStageComplete(Player player, Long stageId) {
    Stage stage = stageRepository.findById(stageId).orElseThrow(() -> {
      LOGGER.info(
          "[recordStageComplete] {}}, stage: {}", ErrorCode.STAGE_NOT_FOUND.getMessage(), stageId);
      return new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
    });

    CompletedStage completedStage;
    if (!completedStageRepository.existsByPlayerIdAndStageId(player.getId(), stageId)) {
      completedStage = CompletedStage.builder()
          .player(player)
          .stage(stage)
          .status(CompletedStatus.COMPLETED)
          .build();
    } else {
      completedStage = completedStageRepository
          .findByPlayerIdAndStageId(player.getId(), stageId)
          .orElseThrow(() -> {
            LOGGER.info(
                "[updateInProgressStage] {}, stage: {}",
                ErrorCode.STAGE_PROGRESS_NOT_FOUND.getMessage(),
                stageId);
            return new BusinessException(ErrorCode.STAGE_PROGRESS_NOT_FOUND, HttpStatus.NOT_FOUND);
          });
      completedStage.updateCompleted();
    }
    completedStage.updateQuestionIndex(stage.getQuestionCount());
    completedStage = completedStageRepository.save(completedStage);

    int updatedExp =
        playerProfileService.increaseExp(player, stage.getDifficultyLevel().getExp());

    StageCompletionResponse response = new StageCompletionResponse(
        player.getExp(),
        player.getExp() / LEVEL_STEP_SIZE,
        updatedExp,
        updatedExp / LEVEL_STEP_SIZE);

    LOGGER.info("[recordStageComplete] player: {}, {} 클리어", player.getUid(), stageId);
    return response;
  }

  @Transactional
  public void recordInProgressStageInit(Player player, GradingRequest request) {
    Stage stage = loadStageByCode(request.stageCode());
    Long stageId = stage.getId();
    if (completedStageRepository.existsByPlayerIdAndStageId(player.getId(), stageId)) {
      return;
    }

    CompletedStage completedStage = CompletedStage.builder()
        .player(player)
        .stage(stage)
        .status(CompletedStatus.IN_PROGRESS)
        .build();

    completedStageRepository.save(completedStage);
    LOGGER.info(
        "[recordInProgressStageInit] 풀이 시작 기록 stage: {}, index: {}",
        stageId,
        request.questionIndex());
  }

  @Transactional
  public void updateInProgressStage(Player player, GradingRequest request) {
    Stage stage = loadStageByCode(request.stageCode());
    Long stageId = stage.getId();
    CompletedStage completedStage = completedStageRepository
        .findByPlayerIdAndStageId(player.getId(), stageId)
        .orElseThrow(() -> {
          LOGGER.info(
              "[updateInProgressStage] {}, stage: {}",
              ErrorCode.STAGE_PROGRESS_NOT_FOUND.getMessage(),
              stageId);
          return new BusinessException(ErrorCode.STAGE_PROGRESS_NOT_FOUND, HttpStatus.NOT_FOUND);
        });

    completedStage.updateQuestionIndex(request.questionIndex());
    completedStageRepository.save(completedStage);
    LOGGER.info(
        "[updateInProgressStage] 진행정도 업데이트 stage: {}, index: {}", stageId, request.questionIndex());
  }

  private Stage loadStageByCode(String stageCode) {
    StageImageType stageImageType;
    try {
      stageImageType = StageImageType.valueOf(stageCode.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND, e);
    }

    return stageRepository
        .findByStageImage(stageImageType)
        .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND));
  }
}
