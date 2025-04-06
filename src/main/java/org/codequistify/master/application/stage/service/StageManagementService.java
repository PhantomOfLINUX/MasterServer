package org.codequistify.master.application.stage.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.lab.service.LabAssignmentService;
import org.codequistify.master.application.player.service.PlayerProfileService;
import org.codequistify.master.application.stage.dto.GradingResponse;
import org.codequistify.master.application.stage.dto.StageActionRequest;
import org.codequistify.master.application.stage.dto.StageCompletionResponse;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.stage.model.CompletedStage;
import org.codequistify.master.core.domain.stage.model.Question;
import org.codequistify.master.core.domain.stage.model.Stage;
import org.codequistify.master.core.domain.stage.model.StageImageType;
import org.codequistify.master.global.util.SuccessResponse;
import org.codequistify.master.infrastructure.stage.converter.CompletedStageConverter;
import org.codequistify.master.infrastructure.stage.converter.QuestionConverter;
import org.codequistify.master.infrastructure.stage.converter.StageConverter;
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
    private final StageRepository stageRepository;

    @Transactional
    public void saveStage(Stage stage) {
        stageRepository.save(StageConverter.toEntity(stage.withStageImage(StageImageType.S0000)));
    }

    @Transactional
    public GradingResponse evaluateAnswer(Player player, Long stageId, int questionIndex, String answer) {
        Question question = getQuestion(stageId, questionIndex);
        Stage stage = question.getStage();

        boolean isCorrect = isAnswerCorrect(question, answer, stage, player);
        boolean isLast = isLastQuestion(stageId, questionIndex);
        boolean isComposable = isComposable(stageId, questionIndex);

        return new GradingResponse(
                isCorrect,
                isLast ? -1 : questionIndex + 1,
                isLast,
                isComposable
        );
    }

    @Transactional
    public SuccessResponse composePShell(Player player, Long stageId, int questionIndex) {
        Stage stage = getQuestion(stageId, questionIndex).getStage();
        return labAssignmentService.sendComposeRequest(
                stage.getStageImage().name(),
                player.getUid(),
                new StageActionRequest(stage.getStageImage().name(), questionIndex)
        );
    }

    @Transactional
    public StageCompletionResponse recordStageComplete(Player player, Long stageId) {
        Stage stage = getStage(stageId);

        CompletedStage completedStage = getCompletedStage(player, stageId)
                .map(CompletedStage::markCompleted)
                .map(s -> s.withQuestionIndex(stage.getQuestionCount()))
                .orElseThrow();

        completedStageRepository.save(CompletedStageConverter.toEntity(completedStage));

        int updatedExp = playerProfileService.increaseExp(player, stage.getDifficultyLevel().getExp());

        return buildCompletionResponse(player.getExp(), updatedExp);
    }

    @Transactional
    public void recordInProgressStageInit(Player player, Long stageId, int index) {
        if (completedStageRepository.existsByPlayerIdAndStageId(player.getUid(), stageId)) {
            return;
        }

        Stage stage = getStage(stageId);
        CompletedStage progress = CompletedStage.builder()
                                                .stage(stage)
                                                .questionIndex(index)
                                                .build()
                                                .markCompleted();

        completedStageRepository.save(CompletedStageConverter.toEntity(progress));
        logger.info("[recordInProgressStageInit] stage: {}, index: {}", stageId, index);
    }

    @Transactional
    public void updateInProgressStage(Player player, Long stageId, int index) {
        CompletedStage stage = getCompletedStage(player, stageId)
                .map(s -> s.withQuestionIndex(index))
                .orElseThrow(() -> new ApplicationException(
                        ErrorCode.STAGE_PROGRESS_NOT_FOUND,
                        HttpStatus.NOT_FOUND));

        completedStageRepository.save(CompletedStageConverter.toEntity(stage));
        logger.info("[updateInProgressStage] 진행 업데이트 - stage: {}, index: {}", stageId, index);
    }

    private Question getQuestion(Long stageId, int index) {
        return questionRepository.findByStageIdAndIndex(stageId, index)
                                 .map(QuestionConverter::toDomain)
                                 .orElseThrow(() -> new ApplicationException(ErrorCode.QUESTION_NOT_FOUND,
                                                                             HttpStatus.NOT_FOUND));
    }

    private Stage getStage(Long stageId) {
        return stageRepository.findById(stageId)
                              .map(StageConverter::toDomain)
                              .orElseThrow(() -> new ApplicationException(ErrorCode.STAGE_NOT_FOUND,
                                                                          HttpStatus.NOT_FOUND));
    }

    private Optional<CompletedStage> getCompletedStage(Player player, Long stageId) {
        return completedStageRepository.findByPlayerIdAndStageId(player.getUid(), stageId)
                                       .map(CompletedStageConverter::toDomain);
    }

    private boolean isAnswerCorrect(Question question, String answer, Stage stage, Player player) {
        if (question.getAnswerType().isStandard()) {
            return question.getCorrectAnswer().equalsIgnoreCase(answer);
        }
        return labAssignmentService.sendGradingRequest(
                stage.getStageImage().name(),
                player.getUid(),
                new StageActionRequest(stage.getStageImage().name(), question.getIndex())
        ).success();
    }

    private boolean isLastQuestion(Long stageId, int index) {
        return !questionRepository.existsByStageIdAndIndex(stageId, index + 1);
    }

    private boolean isComposable(Long stageId, int index) {
        return Optional.ofNullable(
                questionRepository.isComposableForStageAndIndex(stageId, index)
        ).orElse(false);
    }

    private StageCompletionResponse buildCompletionResponse(int oldExp, int newExp) {
        return new StageCompletionResponse(
                oldExp,
                oldExp / LEVEL_STEP_SIZE,
                newExp,
                newExp / LEVEL_STEP_SIZE
        );
    }
}