package org.codequistify.master.domain.stage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.*;
import org.codequistify.master.domain.stage.service.StageService;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.util.BasicResponse;
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
public class StageController {
    private final StageService stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(Stage.class);

    // 스테이지 등록
    @Operation(summary = "스테이지 신규 등록", description = "스테이지 신규 등록")
    @PostMapping("stage")
    @LogMonitoring
    public ResponseEntity<BasicResponse> registryStage(@RequestBody StageRegistryRequest request) {
        stageService.saveStage(request);

        LOGGER.info("[registryStage] 스테이지 등록 완료");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BasicResponse.of("SUCCESS"));
    }

    // 스테이지 목록 조회
    @Operation(
            summary = "스테이지 목록 조회",
            description = """
                    - 스테이지 목록을 페이지 단위로 조회합니다.

                    - 검색가능 조건은 다음과 같습니다. *'스테이지 분류', '세부 난이도', '풀이 여부'*
                    
                    풀이여부
                    - "COMPLETED" : 풀이완료
                    - "NOT_COMPLETED" : 미풀이
                    - "IN_PROGRESS" : 풀이 진행중
                    
                    스테이지 분류
                    - "BASIC_PROBLEMS" : 기본문제
                    - "ADVANCED_PROBLEMS" : 심화문제
                    - "MOCK_TESTS" : 모의고사
                    
                    세부난이도
                    - "L1", "L2", "L3", "L4", "L5"

                    """
    )
    @LogMonitoring
    @GetMapping("stages")
    public ResponseEntity<StagePageResponse> findAllStagesByCriteria(@AuthenticationPrincipal Player player,
                                                                     @Valid @ModelAttribute SearchCriteria searchCriteria) {
        StagePageResponse stages = stageService.findStagesByCriteria(searchCriteria, player);

        return ResponseEntity.status(HttpStatus.OK).body(stages);
    }

    @Operation(
            summary = "문항 조회",
            description = "문항을 조회합니다. 'stage 정보'와 '몇 번째 문항' 인지에 대한 정보로 조회합니다."
    )
    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
    @LogMonitoring
    @GetMapping("stages/{stage_id}/questions/{question_index}")
    public ResponseEntity<?> getQuestionByStage(@PathVariable(name = "stage_id") Long stageId,
                                                @PathVariable(name = "question_index") Integer questionIndex) {
        QuestionResponse response = stageService.findQuestion(stageId, questionIndex);

        LOGGER.info("[getQuestionByStage] 문제 조회 완료 id: {}, index: {}", stageId, questionIndex);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 문항 채점 요청
    @Operation(
            summary = "문항 채점 요청",
            description = "문항을 채점합니다. 문항 고유 Id와 순서에 대한 정보, 채점받을 선택을 요청에 포함합니다."
    )
    @LogMonitoring
    @PostMapping("questions/grading")
    public ResponseEntity<GradingResponse> submitAnswerForGrading(@RequestBody GradingRequest request) {
        GradingResponse response = stageService.checkAnswerCorrectness(request);
        if (response.isCorrect()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 문제 풀이 완료 요청
    // TODO 경험치 제공은 미구현
    @PostMapping("stages/{stageId}/complete")
    @LogMonitoring
    public ResponseEntity<BasicResponse> completeStage(@AuthenticationPrincipal Player player,
                                                       @PathVariable Long stageId,
                                                       @RequestBody StageCompletionRequest request) {

        stageService.recordStageComplete(stageId, player, request.status());

        BasicResponse response = BasicResponse.of("SUCCESS");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    // 스테이지 수정

    // 스테이지 문항 수정

    // 스테이지 옵션 수정
}
