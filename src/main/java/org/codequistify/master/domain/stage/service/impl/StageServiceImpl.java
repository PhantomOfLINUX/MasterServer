package org.codequistify.master.domain.stage.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.convertoer.QuestionConverter;
import org.codequistify.master.domain.stage.convertoer.StageConverter;
import org.codequistify.master.domain.stage.domain.*;
import org.codequistify.master.domain.stage.dto.*;
import org.codequistify.master.domain.stage.repository.CompletedStageRepository;
import org.codequistify.master.domain.stage.repository.QuestionRepository;
import org.codequistify.master.domain.stage.repository.StageRepository;
import org.codequistify.master.domain.stage.service.StageService;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StageServiceImpl implements StageService {
    private final StageRepository stageRepository;
    private final QuestionRepository questionRepository;
    private final CompletedStageRepository completedStageRepository;
    private final JPAQueryFactory queryFactory;

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
    @LogMonitoring
    public StagePageResponse findStagesByCriteria(SearchCriteria searchCriteria, Player player) {
        PageRequest pageRequest = PageRequest.of(searchCriteria.getPage_index()-1, searchCriteria.getPage_size());

        QStage qStage = QStage.stage;
        QCompletedStage qCompletedStage = QCompletedStage.completedStage;

        BooleanBuilder whereClause = new BooleanBuilder();

        // stageGroupTypes 조건 적용
        if (searchCriteria.getStageGroupTypes() != null && !searchCriteria.getStageGroupTypes().isEmpty()) {
            whereClause.and(qStage.stageGroup.in(searchCriteria.getStageGroupTypes()));
        }

        // difficultyLevels 조건 적용
        if (searchCriteria.getDifficultyLevels() != null && !searchCriteria.getDifficultyLevels().isEmpty()) {
            whereClause.and(qStage.difficultyLevel.in(searchCriteria.getDifficultyLevels()));
        }

        // CompletedStatus 조건 적용
        if (searchCriteria.getCompleted() != null) {
            if (searchCriteria.getCompleted() == CompletedStatus.NOT_COMPLETED) {
                // NOT_COMPLETED 상태는 Completed 테이블에 기록되지 않은 상태
                whereClause.and(qCompletedStage.id.isNull());
            } else {
                whereClause.and(qCompletedStage.status.eq(searchCriteria.getCompleted())
                        .and(qCompletedStage.player.id.eq(player.getId())));
            }
        }

        // 조회 쿼리 실행
        List<Stage> results = queryFactory.selectFrom(qStage)
                .leftJoin(qStage.completedStages, qCompletedStage)
                .where(whereClause)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        // 전체 개수 조회
        long total = queryFactory.select(qStage)
                .from(qStage)
                .leftJoin(qStage.completedStages, qCompletedStage)
                .where(whereClause)
                .fetchCount();

        // Page 객체 생성 및 반환
        Page<Stage> pages =  new PageImpl<>(results, pageRequest, total);
        StagePageResponse response = stageConverter.convert(pages);

        LOGGER.info("[findStagesByCriteria] page 조회");
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

    @Transactional
    public Stage findStageById(Long stageId){
        return stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    LOGGER.info("[findStageById] 등록되지 않은 스테이지 id: {}", stageId);
                    return new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
    }

    // 스테이지 등록
    @Override
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


    public List<StageResponseTEMP> findStageWithCompleted(Player player) {
        return stageRepository.findAllByPlayerIdWithCompleted(player.getId());
    }
}
