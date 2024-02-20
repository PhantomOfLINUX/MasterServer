package org.codequistify.master.domain.stage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.*;
import org.codequistify.master.domain.stage.service.StageService;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stage")
@RequestMapping("api")
public class StageController {
    private final StageService stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(Stage.class);

    // 스테이지 등록
    @PostMapping("stage")
    public ResponseEntity<BasicResponse> registryStage(@RequestBody StageRegistryRequest request) {
        stageService.saveStage(request);

        LOGGER.info("[registryStage] 스테이지 등록 완료");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BasicResponse.of("SUCCESS"));
    }

    // 스테이지 목록 조회
    @GetMapping("stages")
    public ResponseEntity<StagePageResponse> findAllStages(@Valid @ModelAttribute SearchCriteria searchCriteria) {
        StagePageResponse stages = stageService.findStageByGroup(searchCriteria);

        return ResponseEntity.status(HttpStatus.OK).body(stages);
    }

    // 스테이지 문제 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
    @GetMapping("stages/{stage_id}/questions/{question_index}")
    public ResponseEntity<?> getQuestionByStage(@PathVariable(name = "stage_id") Long stageId,
                                                @PathVariable(name = "question_index") Integer questionIndex) {
        QuestionResponse response = stageService.findQuestion(stageId, questionIndex);

        LOGGER.info("[getQuestionByStage] 문제 조회 완료 id: {}, index: {}", stageId, questionIndex);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 문제 채점 요청
    @PostMapping("questions/grading")
    public ResponseEntity<GradingResponse> submitAnswerForGrading(@RequestBody GradingRequest request) {
        GradingResponse response = stageService.checkAnswerCorrectness(request);
        if (response.isCorrect()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }



    // 스테이지 수정

    // 스테이지 문항 수정

    // 스테이지 옵션 수정
}
