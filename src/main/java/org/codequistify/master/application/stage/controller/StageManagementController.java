package org.codequistify.master.application.stage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.stage.dto.*;
import org.codequistify.master.application.stage.service.StageManagementService;
import org.codequistify.master.core.domain.player.model.Player;
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
@Tag(name = "org.codequistify.master.core.domain.stageEntity.model.StageEntity")
@RequestMapping("api")
public class StageManagementController {

    private final Logger logger = LoggerFactory.getLogger(StageManagementController.class);
    private final StageManagementService stageManagementService;

    /**
     * 스테이지 등록
     */
    @Operation(summary = "스테이지 신규 등록", description = "스테이지 신규 등록")
    @PostMapping("stages")
    @LogMonitoring
    public ResponseEntity<BasicResponse> registryStage(@RequestBody StageRegistryRequest request) {
        stageManagementService.saveStage(request);
        logger.info("[registryStage] 스테이지 등록 완료");
        return ResponseEntity.ok(BasicResponse.of("SUCCESS"));
    }

    /**
     * 문항 채점 요청
     */
    @Operation(
            summary = "문항 채점 요청",
            description = """
                    문항을 채점합니다. 문항 고유 Id와 순서에 대한 정보, 채점 받을 선택을 요청에 포함합니다.
                    
                    마지막 문항인 경우 `nextIndex = -1`, `isLast = true`를 반환힙니다.
                    
                    다음 문항(questionEntity)이 환경 구성 (compose)을 요구한다면, `isComposable = true` 입니다.
                    """
    )
    @PostMapping("questions/grading")
    @LogMonitoring
    public ResponseEntity<GradingResponse> submitAnswerForGrading(@AuthenticationPrincipal Player player,
                                                                  @Valid @RequestBody GradingRequest request) {
        GradingResponse result = stageManagementService.evaluateAnswer(
                player,
                request.stageId(),
                request.questionIndex(),
                request.answer()
        );

        if (isFirstQuestion(request)) {
            stageManagementService.recordInProgressStageInit(
                    player,
                    request.stageId(),
                    request.questionIndex()
            );
        }

        return result.isCorrect()
                ? handleCorrectAnswer(player, request, result)
                : handleIncorrectAnswer(result);
    }

    private boolean isFirstQuestion(GradingRequest request) {
        return request.questionIndex() == 1;
    }

    private ResponseEntity<GradingResponse> handleCorrectAnswer(Player player,
                                                                GradingRequest request,
                                                                GradingResponse result) {
        stageManagementService.updateInProgressStage(
                player,
                request.stageId(),
                request.questionIndex()
        );
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<GradingResponse> handleIncorrectAnswer(GradingResponse result) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 다음 문항 구성
     */
    @Operation(
            summary = "다음 문제 환경 구성",
            description = "다음 문제를 풀기 위해 필요한 환경을 구성합니다."
    )
    @PostMapping("questionEntity/compose")
    @LogMonitoring
    public ResponseEntity<BasicResponse> compose(@AuthenticationPrincipal Player player,
                                                 @Valid @RequestBody GradingRequest request) {
        boolean success = stageManagementService.composePShell(
                player,
                request.stageId(),
                request.questionIndex()
        ).success();

        if (!success) {
            throw new ApplicationException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(BasicResponse.of("SUCCESS"));
    }

    /**
     * 문제 풀이 완료 요청
     */
    @Operation(
            summary = "스테이지 풀이 완료 기록",
            description = "`stage_id` 에 대한 stageEntity 완료를 기록합니다."
    )
    @PostMapping("stages/{stage_id}/complete")
    @LogMonitoring
    public ResponseEntity<StageCompletionResponse> completeStage(@AuthenticationPrincipal Player player,
                                                                 @PathVariable("stage_id") Long stageId,
                                                                 @RequestBody StageCompletionRequest request) {
        return ResponseEntity.ok(
                stageManagementService.recordStageComplete(player, stageId)
        );
    }

    // TODO: 스테이지 수정 API 추후 구현 예정
}
