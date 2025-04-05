package org.codequistify.master.core.domain.stage.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.convertoer.QuestionConverter;
import org.codequistify.master.core.domain.stage.convertoer.StageConverter;
import org.codequistify.master.core.domain.stage.domain.CompletedStatus;
import org.codequistify.master.core.domain.stage.domain.Question;
import org.codequistify.master.core.domain.stage.domain.Stage;
import org.codequistify.master.core.domain.stage.dto.*;
import org.codequistify.master.core.domain.stage.repository.CompletedStageRepository;
import org.codequistify.master.core.domain.stage.repository.QuestionRepository;
import org.codequistify.master.core.domain.stage.repository.StageRepository;
import org.codequistify.master.core.domain.stage.service.StageSearchService;
import org.codequistify.master.core.domain.stage.utils.HangulExtractor;
import org.codequistify.master.domain.stage.domain.QCompletedStage;
import org.codequistify.master.domain.stage.domain.QStage;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class StageSearchServiceImpl implements StageSearchService {

    private final StageRepository stageRepository;
    private final QuestionRepository questionRepository;
    private final CompletedStageRepository completedStageRepository;
    private final JPAQueryFactory queryFactory;

    private final StageConverter stageConverter;
    private final QuestionConverter questionConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(StageSearchServiceImpl.class);


    @Override // 스테이지 조회
    @Transactional
    public Stage getStageById(Long stageId) {
        return stageRepository.findById(stageId)
                .orElseThrow(() -> {
                    LOGGER.info("[findStageById] 등록되지 않은 스테이지 id: {}", stageId);
                    return new ApplicationException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
    }

    @Override // 문항 조회
    @Transactional
    public QuestionResponse findQuestion(Long stageId, Integer questionIndex) {
        Question question = questionRepository.findByStageIdAndIndex(stageId, questionIndex)
                                              .orElseThrow(() -> {
                    LOGGER.info("[findQuestion] {}, id: {}, index: {}", ErrorCode.QUESTION_NOT_FOUND.getMessage(), stageId, questionIndex);
                                                  return new ApplicationException(ErrorCode.QUESTION_NOT_FOUND,
                                                                                  HttpStatus.NOT_FOUND);
                });
        QuestionResponse response = questionConverter.convert(question);

        LOGGER.info("[findQuestion] 문항 조회, id: {}, index: {}", stageId, questionIndex);
        return response;
    }

    public StageResponse getStageByChoCho(String query) {
        HangulExtractor hangulExtractor = new HangulExtractor();
        String choSrc = hangulExtractor.extractChoseongs(query);

        //int index = 1;
        //PageRequest pageRequest = PageRequest.of(index, 10);

        return stageConverter.convert(
                stageRepository.findAll().stream().parallel()
                        .filter(stage -> {
                            if (hangulExtractor.containsByChoseong(stage.getTitle(), choSrc)) {
                                return true;
                            }
                            if (hangulExtractor.containsByChoseong(stage.getDescription(), choSrc)) {
                                return true;
                            }
                            return false;
                        }).findAny()
                        .orElseThrow(() -> new EntityNotFoundException())
        );
    }

    public StageResponse getStageBySearchText(String query) {
        return stageConverter.convert(
                stageRepository.findAll().stream().parallel()
                        .filter(stage -> {
                            if (stage.getTitle().toLowerCase()
                                    .contains(query.toLowerCase())) {
                                return true;
                            }
                            if (stage.getDescription().toLowerCase()
                                    .contains(query.toLowerCase())) {
                                return true;
                            }
                            if (stage.getStageImage().name().toLowerCase()
                                    .contains(query.toLowerCase())) {
                                return true;
                            }
                            return false;
                        }).findAny()
                        .orElseThrow(() -> new EntityNotFoundException())
        );
    }

    @Override // 완료 스테이지 조회
    @Transactional
    public PlayerStageProgressResponse getCompletedStagesByPlayerId(Long playerId) {
        return new PlayerStageProgressResponse(completedStageRepository.findCompletedStagesByPlayerId(playerId));
    }

    @Override // 진행중 스테이지 조회
    @Transactional
    public PlayerStageProgressResponse getInProgressStagesByPlayerId(Long playerId) {
        return new PlayerStageProgressResponse(completedStageRepository.findInProgressStagesByPlayerId(playerId));
    }

    @Override // 수정일 기준 데이터 조회
    @Transactional
    public List<HeatMapDataPoint> getHeatMapDataPointsByModifiedDate(PolId playerId) {
        return completedStageRepository.countDataByModifiedDate(playerId);
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

        // 임시 스테이지는 제외
        whereClause.and(qStage.approved.eq(true));

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

}
