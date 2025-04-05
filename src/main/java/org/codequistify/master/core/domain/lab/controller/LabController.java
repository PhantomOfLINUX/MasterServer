package org.codequistify.master.core.domain.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.lab.dto.PShellCreateResponse;
import org.codequistify.master.core.domain.lab.dto.PShellExistsResponse;
import org.codequistify.master.core.domain.lab.service.LabService;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.stage.domain.Stage;
import org.codequistify.master.core.domain.stage.service.impl.StageSearchServiceImpl;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequiredArgsConstructor
@Tag(name = "Lab")
public class LabController {
    private final String LAB_HOST = "wss://lab.pol.or.kr";
    private final Logger LOGGER = LoggerFactory.getLogger(LabController.class);
    private final LabService labService;
    private final LockManager lockManager;
    private final StageSearchServiceImpl stageSearchService;

    @Operation(
            summary = "가상 터미널 (PShell) 생성요청",
            description = """
                    :stage에 대한 터미널(PShell)을 생성한다.
                    기존 터미널이 존재할경우, 제거하고 생성한다.
                    생성시에 네트워크 연결까지 약 10초 정도가 소요되며, 
                    제거시에는 약 45s 이상이 소요된다.
                    
                    사용시에는 쿼리파라미터 값을 추가해서 connection 연결을 보내야한다.
                    """
    )
    @LogMonitoring
    @PostMapping("lab/terminal/stage/{stage_id}")
    public ResponseEntity<PShellCreateResponse> applyPShell(@AuthenticationPrincipal Player player,
                                                            @PathVariable(name = "stage_id") Long stageId) {
        ReentrantLock lock = lockManager.getLock(player.getId(), stageId);
        if (lock.tryLock()) {
            try {
                Stage stage = stageSearchService.getStageById(stageId);

                labService.deleteSyncStageOnKubernetes(player, stage); // 동기 삭제
                labService.createStageOnKubernetes(player, stage);

                PShellCreateResponse response = PShellCreateResponse
                        .of(LAB_HOST, player.getUid(), stage.getStageImage().name().toLowerCase());

                labService.waitForPodReadiness(player, stage);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(response);
            } finally {
                lockManager.unlock(player.getId(), stageId);
            }
        }

        // 락 걸린 동안 들어오는 요청은 무시
        LOGGER.info("[applyPShell] apply 작업중 중복된 요청 발생. stage: {}", stageId);
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(null);

    }

    // 접속 가능한 주소 조회
    @Operation(
            summary = "가상 터미널 (PShell) 접속 주소 & 쿼리파라미터 조회",
            description = """
                    :stage에 대한 터미널(PShell)에 접속할 수 있는 쿼리파라미터 정보를 조회한다.
                    
                    사용시에는 쿼리파라미터 값을 추가해서 connection 연결을 보내야한다.
                    """
    )
    @GetMapping("lab/terminal/access-url/{stage_id}")
    @LogMonitoring
    public ResponseEntity<PShellCreateResponse> getPShellAccessUrl(@AuthenticationPrincipal Player player,
                                                                   @PathVariable(name = "stage_id") Long stageId) {
        Stage stage = stageSearchService.getStageById(stageId);

        PShellCreateResponse response = PShellCreateResponse
                .of(LAB_HOST, player.getUid(), stage.getStageImage().name().toLowerCase());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    // 현재 터미널 존재 여부 조회
    @Operation(
            summary = "기존 가상 터미널 (PShell) 존재여부 조회",
            description = """
                    :stage에 대한 기존 터미널(PShell)이 존재하는지를 확인한다.
                    """
    )
    @LogExecutionTime
    @GetMapping("/lab/terminal/existence/{stage_id}")
    public ResponseEntity<PShellExistsResponse> checkPShellExistence(@AuthenticationPrincipal Player player,
                                                                     @PathVariable(name = "stage_id") Long stageId) {
        Stage stage = stageSearchService.getStageById(stageId);

        boolean stageExists = labService.existsStageOnKubernetes(player, stage);

        PShellExistsResponse response = new PShellExistsResponse(
                player.getUid(),
                stageId,
                stage.getStageImage().name(),
                stageExists
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
