package org.codequistify.master.application.stage.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.lab.service.LabAssignmentService;
import org.codequistify.master.application.player.service.PlayerProfileService;
import org.codequistify.master.application.stage.dto.GradingResponse;
import org.codequistify.master.application.stage.dto.StageActionRequest;
import org.codequistify.master.application.stage.dto.StageCompletionResponse;
import org.codequistify.master.application.stage.dto.StageRegistryRequest;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.stage.model.CompletedStatus;
import org.codequistify.master.core.domain.stage.model.StageImageType;
import org.codequistify.master.global.util.SuccessResponse;
import org.codequistify.master.infrastructure.player.converter.PlayerConverter;
import org.codequistify.master.infrastructure.stage.converter.StageConverter;
import org.codequistify.master.infrastructure.stage.entity.CompletedStageEntity;
import org.codequistify.master.infrastructure.stage.entity.QuestionEntity;
import org.codequistify.master.infrastructure.stage.entity.StageEntity;
import org.codequistify.master.infrastructure.stage.repository.CompletedStageRepository;
import org.codequistify.master.infrastructure.stage.repository.QuestionRepository;
import org.codequistify.master.infrastructure.stage.repository.StageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StageManagementService {

    private static final int LEVEL_STEP_SIZE = 500;
    private static final Logger logger = LoggerFactory.getLogger(StageManagementService.class);
    private final CompletedStageRepository completedStageRepository;
    private final LabAssignmentService labAssignmentService;
    private final PlayerProfileService playerProfileService;
    private final QuestionRepository questionRepository;
    private final StageConverter stageConverter;
    private final StageRepository stageRepository;

    @Transactional
    public void saveStage(StageRegistryRequest request) {
        StageEntity stageEntity = stageConverter.convert(request);
        stageEntity.updateStageImage(StageImageType.S0000);
        stageRepository.save(stageEntity);
    }

    @Transactional
    public GradingResponse evaluateAnswer(Player player, Long stageId, int questionIndex, String answer) {
        QuestionEntity questionEntity = findQuestion(stageId, questionIndex);
        StageEntity stageEntity = questionEntity.getStageEntity();

        boolean isCorrect = isAnswerCorrect(player, questionEntity, stageEntity, questionIndex, answer);
        boolean isLast = isLastQuestion(stageId, questionIndex);
        boolean isComposable = isComposableQuestion(stageId, questionIndex);
        int nextIndex = isLast ? -1 : questionIndex + 1;

        return new GradingResponse(isCorrect, nextIndex, isLast, isComposable);
    }

    private boolean isAnswerCorrect(Player player,
                                    QuestionEntity questionEntity,
                                    StageEntity stageEntity,
                                    int index,
                                    String answer) {
        return questionEntity.getAnswerType().isStandard()
                ? isStandardAnswerCorrect(questionEntity, answer)
                : isPracticalAnswerCorrect(player, stageEntity, index);
    }

    private boolean isStandardAnswerCorrect(QuestionEntity questionEntity, String answer) {
        return questionEntity.getCorrectAnswer().equalsIgnoreCase(answer);
    }

    private boolean isPracticalAnswerCorrect(Player player, StageEntity stageEntity, int index) {
        StageActionRequest action = new StageActionRequest(stageEntity.getStageImage().name(), index);
        return labAssignmentService.sendGradingRequest(stageEntity.getStageImage().name(), player.getUid(), action)
                                   .success();
    }

    private boolean isLastQuestion(Long stageId, int index) {
        return !questionRepository.existsByStageIdAndIndex(stageId, index + 1);
    }

    private boolean isComposableQuestion(Long stageId, int index) {
        return Optional.ofNullable(questionRepository.isComposableForStageAndIndex(stageId, index)).orElse(false);
    }

    @Transactional
    public SuccessResponse composePShell(Player player, Long stageId, int index) {
        QuestionEntity questionEntity = findQuestion(stageId, index);
        StageEntity stageEntity = questionEntity.getStageEntity();
        StageActionRequest action = new StageActionRequest(stageEntity.getStageImage().name(), index);

        return labAssignmentService.sendComposeRequest(stageEntity.getStageImage().name(), player.getUid(), action);
    }

    @Transactional
    public StageCompletionResponse recordStageComplete(Player player, Long stageId) {
        StageEntity stageEntity = findStage(stageId);
        CompletedStageEntity completedStageEntity = loadOrInitCompletedStage(player, stageId, stageEntity);

        completedStageEntity.updateQuestionIndex(stageEntity.getQuestionCount());
        completedStageRepository.save(completedStageEntity);

        int updatedExp = playerProfileService.increaseExp(player, stageEntity.getDifficultyLevel().getExp());

        logger.info("[recordStageComplete] player: {}, stageEntity: {} 클리어", player.getUid(), stageId);

        return new StageCompletionResponse(
                player.getExp(),
                player.getExp() / LEVEL_STEP_SIZE,
                updatedExp,
                updatedExp / LEVEL_STEP_SIZE
        );
    }

    private CompletedStageEntity loadOrInitCompletedStage(Player player, Long stageId, StageEntity stageEntity) {
        if (!completedStageRepository.existsByPlayerIdAndStageId(player.getUid(), stageId)) {
            return CompletedStageEntity.builder()
                                       .player(PlayerConverter.toEntity(player))
                                       .stage(stageEntity)
                                       .status(CompletedStatus.COMPLETED)
                                       .build();
        }

        return completedStageRepository.findByPlayerIdAndStageId(player.getUid(), stageId)
                                       .map(this::markAsCompleted)
                                       .orElseThrow(() -> new ApplicationException(ErrorCode.STAGE_PROGRESS_NOT_FOUND,
                                                                                   HttpStatus.NOT_FOUND));
    }

    private CompletedStageEntity markAsCompleted(CompletedStageEntity stage) {
        stage.updateCompleted();
        return stage;
    }

    @Transactional
    public void recordInProgressStageInit(Player player, Long stageId, int index) {
        if (completedStageRepository.existsByPlayerIdAndStageId(player.getUid(), stageId)) {
            return;
        }

        StageEntity stageEntity = findStage(stageId);
        CompletedStageEntity progress = CompletedStageEntity.builder()
                                                            .player(PlayerConverter.toEntity(player))
                                                            .stage(stageEntity)
                                                            .status(CompletedStatus.IN_PROGRESS)
                                                            .build();

        completedStageRepository.save(progress);
        logger.info("[recordInProgressStageInit] 풀이 시작 - stageEntity: {}, index: {}", stageId, index);
    }

    @Transactional
    public void updateInProgressStage(Player player, Long stageId, int index) {
        CompletedStageEntity stage = completedStageRepository.findByPlayerIdAndStageId(player.getUid(), stageId)
                                                             .orElseThrow(() -> new ApplicationException(ErrorCode.STAGE_PROGRESS_NOT_FOUND,
                                                                                                         HttpStatus.NOT_FOUND));

        stage.updateQuestionIndex(index);
        completedStageRepository.save(stage);

        logger.info("[updateInProgressStage] 진행 업데이트 - stageEntity: {}, index: {}", stageId, index);
    }

    private QuestionEntity findQuestion(Long stageId, int index) {
        return questionRepository.findByStageIdAndIndex(stageId, index)
                                 .orElseThrow(() -> new ApplicationException(ErrorCode.QUESTION_NOT_FOUND,
                                                                             HttpStatus.NOT_FOUND));
    }

    private StageEntity findStage(Long stageId) {
        return stageRepository.findById(stageId)
                              .orElseThrow(() -> new ApplicationException(ErrorCode.STAGE_NOT_FOUND,
                                                                          HttpStatus.NOT_FOUND));
    }
}
