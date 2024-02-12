package org.codequistify.master.domain.stage.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.dto.StageRegistryRequest;
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
@RequestMapping("api/stage")
public class StageController {
    private final StageService stageService;
    private final Logger LOGGER = LoggerFactory.getLogger(Stage.class);
    // 스테이지 목록 조회
    @GetMapping()
    public ResponseEntity<Void> getStagePage(@RequestParam Long page, @RequestParam Long size) {
        return null;
    }

    // 스테이지 문항 조회 -> 정답은 클라이언트에게 제공 안 됨, 옵션들은 전부 제공되어야 함

    // 문항 체점 요청

    // 스테이지 등록
    @PostMapping()
    public ResponseEntity<BasicResponse> registryStage(@RequestBody StageRegistryRequest request) {
        stageService.saveStage(request);

        LOGGER.info("[registryStage] 스테이지 등록 완료");
        BasicResponse response = new BasicResponse("SUCCESS", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 스테이지 수정

    // 스테이지 문항 수정

    // 스테이지 옵션 수정
}
