package org.codequistify.master.domain.stage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.dto.GradingRequest;
import org.codequistify.master.domain.stage.dto.GradingResponse;
import org.codequistify.master.domain.stage.dto.StageCompletionRequest;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.codequistify.master.domain.stage.service.StageManagementService;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.util.BasicResponse;
import org.codequistify.master.global.util.SuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Stage")
@RequestMapping("api")
public class StageManagementController {
    private final StageManagementService stageManagementService;
    private final Logger LOGGER = LoggerFactory.getLogger(StageManagementController.class);

    // 스테이지 등록
    @Operation(summary = "스테이지 신규 등록", description = "스테이지 신규 등록")
    @PostMapping("stages")
    @LogMonitoring
    public ResponseEntity<BasicResponse> registryStage(@RequestBody StageRegistryRequest request) {
        stageManagementService.saveStage(request);

        LOGGER.info("[registryStage] 스테이지 등록 완료");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BasicResponse.of("SUCCESS"));
    }

    // 문항 채점 요청
    @Operation(
            summary = "문항 채점 요청",
            description = "문항을 채점합니다. 문항 고유 Id와 순서에 대한 정보, 채점받을 선택을 요청에 포함합니다."
    )
    @LogMonitoring
    @PostMapping("questions/grading")
    public ResponseEntity<GradingResponse> submitAnswerForGrading(@AuthenticationPrincipal Player player,
                                                                  @Valid @RequestBody GradingRequest request) {

        GradingResponse response = stageManagementService.evaluateAnswer(player, request);
        if (response.isCorrect()) {
            stageManagementService.updateInProgressStage(player, request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        if (request.questionIndex() == 1) { // 첫문제인 경우 진행 시작 기록
            stageManagementService.recordInProgressStageInit(player, request);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    //다음 문항 구성
    @Operation(
            summary = "다음 문제 환경 구성",
            description = "다음 문제를 풀기 위해 필요한 환경을 구성합니다."
    )
    @LogMonitoring
    @PostMapping("question/compose")
    public ResponseEntity<BasicResponse> compose(@AuthenticationPrincipal Player player,
                                                 @Valid @RequestBody GradingRequest request) {
        SuccessResponse successResponse = stageManagementService.composePShell(player, request);

        if (successResponse.success().equals(false)) {
            throw new BusinessException(ErrorCode.FAIL_PROCEED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        BasicResponse response = BasicResponse.of("SUCCESS");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    // 문제 풀이 완료 요청
    // TODO 경험치 제공은 미구현
    @PostMapping("stages/{stageId}/complete")
    @LogMonitoring
    public ResponseEntity<BasicResponse> completeStage(@AuthenticationPrincipal Player player,
                                                       @PathVariable Long stageId,
                                                       @RequestBody StageCompletionRequest request) {

        stageManagementService.recordStageComplete(player, stageId);

        BasicResponse response = BasicResponse.of("SUCCESS");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    // 스테이지 수정

    // 스테이지 문항 수정

    // 스테이지 옵션 수정


}
