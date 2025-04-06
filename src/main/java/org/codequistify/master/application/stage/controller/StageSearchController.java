package org.codequistify.master.application.stage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.stage.dto.QuestionResponse;
import org.codequistify.master.application.stage.dto.SearchCriteria;
import org.codequistify.master.application.stage.dto.StagePageResponse;
import org.codequistify.master.application.stage.dto.StageResponse;
import org.codequistify.master.application.stage.service.StageSearchService;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.stage.utils.HangulExtractor;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "stage")
@RequestMapping("api")
public class StageSearchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StageSearchController.class);
    private final HangulExtractor hangulExtractor;
    private final StageSearchService stageSearchService;

    @Operation(
            summary = "스테이지 목록 조회",
            description = """
                    - 스테이지 목록을 페이지 단위로 조회합니다.
                    - 조건: 스테이지 분류, 난이도, 풀이 여부, 텍스트 포함 여부
                    """
    )
    @LogExecutionTime
    @GetMapping("stages")
    public ResponseEntity<StagePageResponse> findAllStagesByCriteria(
            @AuthenticationPrincipal Player player,
            @Valid @ModelAttribute SearchCriteria searchCriteria) {

        return ResponseEntity.ok(stageSearchService.findStagesByCriteria(searchCriteria, player));
    }

    @Operation(
            summary = "스테이지 조회",
            description = "스테이지를 조회합니다. completedStatus는 항상 null입니다."
    )
    @LogExecutionTime
    @GetMapping("stages/{stage_id}")
    public ResponseEntity<StageResponse> getStageInfoById(@PathVariable("stage_id") Long stageId) {
        return ResponseEntity.ok(StageResponse.from(stageSearchService.getStageById(stageId)));
    }

    @Operation(
            summary = "문항 조회",
            description = "해당 스테이지의 문항을 조회합니다."
    )
    @LogMonitoring
    @GetMapping("stages/{stage_id}/questions/{question_index}")
    public ResponseEntity<QuestionResponse> getQuestionByStage(@PathVariable("stage_id") Long stageId,
                                                               @PathVariable("question_index") Integer questionIndex) {
        LOGGER.info("[getQuestionByStage] 문제 조회 완료 id: {}, index: {}", stageId, questionIndex);
        return ResponseEntity.ok(stageSearchService.findQuestion(stageId, questionIndex));
    }

    @GetMapping("/chocho")
    public ResponseEntity<HangulExtractor.ChoCho> hangulQuery(@RequestParam String src) {
        return Optional.ofNullable(src)
                       .map(hangulExtractor::extractChoseongs)
                       .map(HangulExtractor.ChoCho::new)
                       .map(ResponseEntity::ok)
                       .orElse(ResponseEntity.badRequest().build());
    }

    @Operation(
            summary = "쿼리와 일치하는 스테이지 1개 조회",
            description = """
                    - 일치하는 스테이지를 하나 반환합니다.
                    - 초성 포함 시 초성 기준으로 조회
                    - 없으면 204 No Content
                    """
    )
    @LogMonitoring
    @GetMapping("stages/preview/search")
    public ResponseEntity<StageResponse> searchStagePreview(@RequestParam String query) {
        try {
            return HangulExtractor.CHOSEONGS.stream()
                                            .filter(ch -> query.contains(ch.toString()))
                                            .findFirst()
                                            .map(__ -> stageSearchService.getStageByChoCho(query))
                                            .map(ResponseEntity::ok)
                                            .or(() -> Optional.of(stageSearchService.getStageBySearchText(query))
                                                              .map(ResponseEntity::ok))
                                            .orElse(ResponseEntity.noContent().build());
        } catch (EntityNotFoundException e) {
            LOGGER.info("[searchStagePreview] 조회된 stage가 존재하지 않습니다.");
            return ResponseEntity.noContent().build();
        }
    }
}
