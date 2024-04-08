package org.codequistify.master.domain.stage.service.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.convertoer.QuestionConverter;
import org.codequistify.master.domain.stage.convertoer.StageConverter;
import org.codequistify.master.domain.stage.domain.CompletedStage;
import org.codequistify.master.domain.stage.domain.CompletedStatus;
import org.codequistify.master.domain.stage.domain.Question;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.GradingRequest;
import org.codequistify.master.domain.stage.dto.GradingResponse;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.codequistify.master.domain.stage.repository.CompletedStageRepository;
import org.codequistify.master.domain.stage.repository.QuestionRepository;
import org.codequistify.master.domain.stage.repository.StageRepository;
import org.codequistify.master.domain.stage.service.StageManagementService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
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
    private final JPAQueryFactory queryFactory;

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
    public GradingResponse checkAnswerCorrectness(GradingRequest request) {
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> {
                    LOGGER.info("");
                    return new BusinessException(ErrorCode.UNKNOWN, HttpStatus.BAD_REQUEST);
                });

        String correctAnswer = question.getCorrectAnswer();
        boolean isCorrect = correctAnswer.equalsIgnoreCase(request.answer());
        boolean isLast = !questionRepository.existsByIndex(request.questionIndex() + 1);
        int nextIndex = isLast ? -1 : request.questionIndex() + 1;

        return new GradingResponse(
                isCorrect,
                nextIndex,
                isLast
        );
    }

    // 스테이지 등록
    @Override
    @Transactional
    public void recordStageComplete(Long stageId, Player player, CompletedStatus status) {
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    LOGGER.info("[recordStageComplete] 등록되지 않은 stage에 대한 등록, stage: {}", stageId);
                    return new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                });

        CompletedStage completedStage = CompletedStage.builder()
                .player(player)
                .stage(stage)
                .status(status).build();

        completedStage = completedStageRepository.save(completedStage);
        LOGGER.info("[recordStageComplete] player: {}, {} 클리어", player.getUid(), stageId);

    }


}
