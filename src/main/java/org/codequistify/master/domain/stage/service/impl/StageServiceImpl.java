package org.codequistify.master.domain.stage.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerStageProgressResponse;
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

    @Override // 입력 쿼리를 기반으로 일치하는 문제 조건 검색
    @Transactional
    @LogMonitoring
    public StagePageResponse findStagesByCriteria(SearchCriteria searchCriteria, Player player) {
        PageRequest pageRequest = PageRequest
                .of(searchCriteria.getPage_index() - 1, searchCriteria.getPage_size());

        QStage qStage = QStage.stage;
        QCompletedStage qCompletedStage = QCompletedStage.completedStage;

        BooleanBuilder whereClause = new BooleanBuilder();

        // stageGroupType 조건 적용
        this.addWhereConditionForStageGroupTypes(whereClause, searchCriteria, qStage);

        // difficultLevels 조건 적용
        this.addWhereConditionForDifficultLevels(whereClause, searchCriteria, qStage);

        // searchText 조건 적용
        this.addWhereConditionForSearchText(whereClause, searchCriteria, qStage);

        // CompletedStatus 조건 적용
        this.addWhereConditionForCompletedStatus(whereClause, searchCriteria, qCompletedStage, player.getId());

        // 쿼리 결과 조회
        List<StageResponse> results = fetchStageResponses(whereClause, qStage, qCompletedStage, pageRequest, player.getId());

        // 전체 개수 조회
        long total = fetchTotalCount(whereClause, qStage, qCompletedStage, player.getId());

        // Page 객체 생성 및 반환
        Page<StageResponse> pages = new PageImpl<>(results, pageRequest, total);
        PageParameters pageParameters = PageParameters.of(
                pages.getTotalPages(),
                pages.getSize(),
                pages.getNumber() + 1,
                pages.getNumberOfElements(),
                (int) pages.getTotalElements()
        );


        StagePageResponse response = StagePageResponse.of(pages.getContent(), pageParameters);

        LOGGER.info("[findStagesByCriteria] page 조회");
        return response;
    }

    private List<StageResponse> fetchStageResponses(
            BooleanBuilder whereClause,
            QStage qStage,
            QCompletedStage qCompletedStage,
            PageRequest pageRequest,
            Long playerId) {

        return queryFactory
                .select(Projections.constructor(StageResponse.class,
                        qStage.id,
                        Expressions.stringTemplate("cast({0} as string)", qStage.stageImage),
                        qStage.title,
                        qStage.description,
                        qStage.stageGroup,
                        qStage.difficultyLevel,
                        qStage.questionCount,
                        qCompletedStage.status.coalesce(CompletedStatus.NOT_COMPLETED)))
                .from(qStage)
                .leftJoin(qStage.completedStages, qCompletedStage)
                .on(qCompletedStage.player.id.eq(playerId))
                .where(whereClause)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();
    }

    private long fetchTotalCount(BooleanBuilder whereClause,
                                 QStage qStage,
                                 QCompletedStage qCompletedStage,
                                 Long playerId) {
        return queryFactory
                .from(qStage)
                .leftJoin(qStage.completedStages, qCompletedStage)
                .on(qCompletedStage.player.id.eq(playerId))
                .where(whereClause)
                .fetchCount();
    }


    private void addWhereConditionForStageGroupTypes(BooleanBuilder whereClause, SearchCriteria searchCriteria, QStage qStage) {
        if (searchCriteria.getStageGroupTypes() == null || searchCriteria.getStageGroupTypes().isEmpty()) {
            return;
        }
        whereClause.and(qStage.stageGroup.in(searchCriteria.getStageGroupTypes()));
    }

    private void addWhereConditionForDifficultLevels(BooleanBuilder whereClause, SearchCriteria searchCriteria, QStage qStage) {
        if (searchCriteria.getDifficultyLevels() == null || searchCriteria.getDifficultyLevels().isEmpty()) {
            return;
        }
        whereClause.and(qStage.difficultyLevel.in(searchCriteria.getDifficultyLevels()));
    }

    private void addWhereConditionForSearchText(BooleanBuilder whereClause, SearchCriteria searchCriteria, QStage qStage) {
        if (searchCriteria.getSearchText() == null || searchCriteria.getSearchText().isBlank()) {
            return;
        }
        BooleanExpression descriptionContains = qStage.description
                .contains(searchCriteria.getSearchText());

        BooleanExpression titleContains = qStage.title
                .contains(searchCriteria.getSearchText());

        BooleanExpression stageImageContains = Expressions
                .stringTemplate("cast({0} as string)", qStage.stageImage)
                .contains(searchCriteria.getSearchText());

        whereClause.and(descriptionContains.or(titleContains).or(stageImageContains));
    }

    private void addWhereConditionForCompletedStatus(BooleanBuilder whereClause, SearchCriteria searchCriteria, QCompletedStage qCompletedStage, Long playerId) {
        if (searchCriteria.getCompleted() != null) {
            if (searchCriteria.getCompleted() == CompletedStatus.NOT_COMPLETED) {
                // NOT_COMPLETED 상태는 Completed 테이블에 기록되지 않은 상태임
                whereClause.and(qCompletedStage.id.isNull());
            } else {
                whereClause.and(qCompletedStage.status.eq(searchCriteria.getCompleted())
                        .and(qCompletedStage.player.id.eq(playerId)));
            }
        }
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
        boolean isLast = !questionRepository.existsByIndex(request.questionIndex() + 1);
        int nextIndex = isLast ? -1 : request.questionIndex() + 1;

        return new GradingResponse(
                isCorrect,
                nextIndex,
                isLast
        );
    }

    @Transactional
    public Stage findStageById(Long stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    LOGGER.info("[findStageById] 등록되지 않은 스테이지 id: {}", stageId);
                    return new BusinessException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
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

    @Override
    @Transactional
    public PlayerStageProgressResponse getCompletedStagesByPlayerId(Long playerId) {
        return new PlayerStageProgressResponse(completedStageRepository.findCompletedStagesByPlayerId(playerId));
    }

    @Override
    @Transactional
    public PlayerStageProgressResponse getInProgressStagesByPlayerId(Long playerId) {
        return new PlayerStageProgressResponse(completedStageRepository.findInProgressStagesByPlayerId(playerId));
    }
}
