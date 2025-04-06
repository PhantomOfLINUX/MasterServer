package org.codequistify.master.application.stage.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.application.stage.dto.*;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.stage.model.Question;
import org.codequistify.master.core.domain.stage.model.Stage;
import org.codequistify.master.core.domain.stage.model.StageSearchCondition;
import org.codequistify.master.core.domain.stage.utils.HangulExtractor;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.infrastructure.stage.converter.QuestionConverter;
import org.codequistify.master.infrastructure.stage.converter.StageConverter;
import org.codequistify.master.infrastructure.stage.entity.QuestionEntity;
import org.codequistify.master.infrastructure.stage.repository.CompletedStageRepository;
import org.codequistify.master.infrastructure.stage.repository.QuestionRepository;
import org.codequistify.master.infrastructure.stage.repository.StageQueryRepository;
import org.codequistify.master.infrastructure.stage.repository.StageRepository;
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
public class StageSearchService {

    private final CompletedStageRepository completedStageRepository;
    private final Logger logger = LoggerFactory.getLogger(StageSearchService.class);
    private final QuestionRepository questionRepository;
    private final StageQueryRepository stageQueryRepository;
    private final StageRepository stageRepository;

    @Transactional(readOnly = true)
    public Stage getStageById(Long stageId) {
        return StageConverter.toDomain(
                stageRepository.findById(stageId)
                               .orElseThrow(() -> {
                                   logger.info("[getStageById] 등록되지 않은 스테이지 id: {}", stageId);
                                   return new ApplicationException(ErrorCode.STAGE_NOT_FOUND, HttpStatus.NOT_FOUND);
                               }));
    }

    @Transactional(readOnly = true)
    public Question findQuestion(Long stageId, Integer questionIndex) {
        QuestionEntity questionEntity = questionRepository.findByStageIdAndIndex(stageId, questionIndex)
                                                          .orElseThrow(() -> {
                                                              logger.info("[findQuestion] {}, id: {}, index: {}",
                                                                          ErrorCode.QUESTION_NOT_FOUND.getMessage(),
                                                                          stageId, questionIndex);
                                                              return new ApplicationException(ErrorCode.QUESTION_NOT_FOUND,
                                                                                              HttpStatus.NOT_FOUND);
                                                          });

        logger.info("[findQuestion] 문항 조회, id: {}, index: {}", stageId, questionIndex);
        return QuestionConverter.toDomain(questionEntity);
    }

    @Transactional(readOnly = true)
    public Stage getStageByChoCho(String query) {
        HangulExtractor extractor = new HangulExtractor();
        String choSrc = extractor.extractChoseongs(query);

        return stageRepository.findAll().stream()
                              .parallel()
                              .filter(stage ->
                                              extractor.containsByChoseong(stage.getTitle(), choSrc) ||
                                                      extractor.containsByChoseong(stage.getDescription(), choSrc))
                              .findAny()
                              .map(StageConverter::toDomain)
                              .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Stage getStageBySearchText(String query) {
        String lowerQuery = query.toLowerCase();

        return stageRepository.findAll().stream()
                              .parallel()
                              .filter(stage ->
                                              stage.getTitle().toLowerCase().contains(lowerQuery) ||
                                                      stage.getDescription().toLowerCase().contains(lowerQuery) ||
                                                      stage.getStageImage().name().toLowerCase().contains(lowerQuery))
                              .findAny()
                              .map(StageConverter::toDomain)
                              .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public PlayerStageProgressResponse getCompletedStagesByPlayerId(PolId uid) {
        return new PlayerStageProgressResponse(completedStageRepository.findCompletedStagesByPlayerId(uid));
    }

    @Transactional(readOnly = true)
    public PlayerStageProgressResponse getInProgressStagesByPlayerId(PolId uid) {
        return new PlayerStageProgressResponse(completedStageRepository.findInProgressStagesByPlayerId(uid));
    }

    @Transactional(readOnly = true)
    public List<HeatMapDataPoint> getHeatMapDataPointsByModifiedDate(PolId uid) {
        return completedStageRepository.countDataByModifiedDate(uid);
    }

    @Transactional(readOnly = true)
    @LogMonitoring
    public StagePageResponse findStagesByCriteria(SearchCriteria criteria, Player player) {
        PageRequest pageRequest = PageRequest.of(criteria.getPage_index() - 1, criteria.getPage_size());

        StageSearchCondition condition = StageSearchCondition.builder()
                                                             .stageGroupTypes(criteria.getStageGroupTypes())
                                                             .difficultyLevels(criteria.getDifficultyLevels())
                                                             .searchText(criteria.getSearchText())
                                                             .completedStatus(criteria.getCompleted())
                                                             .build();

        List<StageResponse> content = stageQueryRepository.findStages(condition, pageRequest, player.getUid());
        long total = stageQueryRepository.countStages(condition, player.getUid());

        Page<StageResponse> page = new PageImpl<>(content, pageRequest, total);
        PageParameters pageParameters = PageParameters.of(
                page.getTotalPages(),
                page.getSize(),
                page.getNumber() + 1,
                page.getNumberOfElements(),
                (int) page.getTotalElements()
        );

        logger.info("[findStagesByCriteria] page 조회");
        return StagePageResponse.of(page.getContent(), pageParameters);
    }
}
