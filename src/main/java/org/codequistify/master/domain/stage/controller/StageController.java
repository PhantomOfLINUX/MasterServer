package org.codequistify.master.domain.stage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.SearchCriteria;
import org.codequistify.master.domain.stage.dto.StagePageResponse;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
import org.codequistify.master.domain.stage.service.impl.StageServiceImpl;
import org.codequistify.master.global.util.BasicResponse;
import org.codequistify.master.global.util.Validator;
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
    private final StageServiceImpl stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(Stage.class);
    private final Validator validator;
    // 스테이지 목록 조회
    @GetMapping("stages/page")
    public ResponseEntity<StagePageResponse> getStagePage(@ModelAttribute SearchCriteria searchCriteria) {
        validator.isValid(searchCriteria);

        StagePageResponse stages = stageService.findStageByGroup(searchCriteria);

        return ResponseEntity.status(HttpStatus.OK).body(stages);
    }

    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함
    @GetMapping("stages/{stage_id}/questions")
    public ResponseEntity<?> getQuestions(@PathVariable(name = "stage_id") Long stageId) {
        return null;
    }

    // 문항 체점 요청

    // 스테이지 등록
    @PostMapping()
    public ResponseEntity<BasicResponse> registryStage(@RequestBody StageRegistryRequest request) {
        stageService.saveStage(request);

        LOGGER.info("[registryStage] 스테이지 등록 완료");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BasicResponse.of("SUCCESS"));
    }

    // 스테이지 수정

    // 스테이지 문항 수정

    // 스테이지 옵션 수정
}
