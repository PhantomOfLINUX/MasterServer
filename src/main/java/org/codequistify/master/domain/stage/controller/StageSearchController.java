package org.codequistify.master.domain.stage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.dto.QuestionResponse;
import org.codequistify.master.domain.stage.dto.SearchCriteria;
import org.codequistify.master.domain.stage.dto.StagePageResponse;
import org.codequistify.master.domain.stage.dto.StageResponse;
import org.codequistify.master.domain.stage.service.StageManagementService;
import org.codequistify.master.domain.stage.service.StageSearchService;
import org.codequistify.master.domain.stage.utils.HangulExtractor;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stage")
@RequestMapping("api")
public class StageSearchController {
    private final StageManagementService stageManagementService;
    private final StageSearchService stageSearchService;
    private final Logger LOGGER = LoggerFactory.getLogger(StageSearchController.class);
    // 스테이지 목록 조회
    @Operation(
            summary = "스테이지 목록 조회",
            description = """
                    - 스테이지 목록을 페이지 단위로 조회합니다.

                    - 검색가능 조건은 다음과 같습니다. *'스테이지 분류', '세부 난이도', '풀이 여부', '검색 텍스트 포함'*
                    
                    *풀이여부*
                    - completedStatus 와 일치하는 상태를 가지는 문제를 필터링합니다.
                    - "COMPLETED" : 풀이완료
                    - "NOT_COMPLETED" : 미풀이
                    - "IN_PROGRESS" : 풀이 진행중
                    
                    *스테이지 분류*
                    - stageGroupTypes 과 일치하는 타입을 가지는 문제를 필터링합니다.
                    - "BASIC_PROBLEMS" : 기본문제
                    - "ADVANCED_PROBLEMS" : 심화문제
                    - "MOCK_TESTS" : 모의고사
                    
                    *세부난이도*
                    - difficultLevels 배열안에 있는 난이도를 가지는 문제를 필터링합니다.
                    - "L1", "L2", "L3", "L4", "L5"
                    
                    *텍스트 검색*
                    - searchText 가 'title', 'description', 'stageCode' 중 일치하는 문제를 필터링합니다

                    """
    )
    @LogMonitoring
    @GetMapping("stages")
    public ResponseEntity<StagePageResponse> findAllStagesByCriteria(@AuthenticationPrincipal Player player,
                                                                     @Valid @ModelAttribute SearchCriteria searchCriteria) {
        StagePageResponse stages = stageSearchService.findStagesByCriteria(searchCriteria, player);

        return ResponseEntity.status(HttpStatus.OK).body(stages);
    }

    @Operation(
            summary = "문항 조회",
            description = "문항을 조회합니다. 'stage 정보'와 '몇 번째 문항' 인지에 대한 정보로 조회합니다."
    )
    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
    @LogMonitoring
    @GetMapping("stages/{stage_id}/questions/{question_index}")
    public ResponseEntity<QuestionResponse> getQuestionByStage(@PathVariable(name = "stage_id") Long stageId,
                                                @PathVariable(name = "question_index") Integer questionIndex) {
        QuestionResponse response = stageSearchService.findQuestion(stageId, questionIndex);

        LOGGER.info("[getQuestionByStage] 문제 조회 완료 id: {}, index: {}", stageId, questionIndex);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 초성 검색
    @GetMapping("/chocho")
    public ResponseEntity<HangulExtractor.ChoCho> HangulQuery(@RequestParam String src) {
        HangulExtractor hangulExtractor = new HangulExtractor();
        HangulExtractor.ChoCho response = new HangulExtractor
                .ChoCho(hangulExtractor
                .extractChoseongs(src));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "쿼리랑 일치하는 스테이지 1개 조회",
            description = "쿼리랑 일치하는 스테이지를 딱 1개 조회합니다.\n\n" +
                    " 일치하는 결과가 여래개인 경우, 결과가 항상 다를 수 있습니다.\n\n" +
                    "일치하는 결과가 없을 경우 204 NO_CONTENT 를 반환합니다."
    )
    @LogMonitoring
    @GetMapping("stages/preview/search")
    public ResponseEntity<StageResponse> searchStagePreview(@RequestParam String query) {
        StageResponse response;
        try {
            for (Character ch : HangulExtractor.choseongs) {
                if (query.contains(ch.toString())) { // 한글 초성이 존재할 때
                    response = stageSearchService.getStageByChoCho(query);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }
            }
            response = stageSearchService.getStageBySearchText(query);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch (EntityNotFoundException exception) {
            LOGGER.info("[searchStagePreview] 조회된 stage가 존재하지 않습니다.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
    }
}
