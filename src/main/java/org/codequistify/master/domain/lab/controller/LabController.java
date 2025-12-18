package org.codequistify.master.domain.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.virtualworkspace.application.VirtualWorkspaceApplicationService;
import org.codequistify.master.domain.lab.virtualworkspace.dto.VirtualWorkspaceConnectResponse;
import org.codequistify.master.domain.lab.virtualworkspace.dto.VirtualWorkspaceExistenceResponse;
import org.codequistify.master.domain.player.domain.Player;
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
    private final VirtualWorkspaceApplicationService virtualWorkspaceApplicationService;
    private final LockManager lockManager;

    private final Logger LOGGER = LoggerFactory.getLogger(LabController.class);

    @Operation(
            summary = "가상 작업공간 (VirtualWorkspace) 생성요청",
            description = """
                    :stage에 대한 VirtualWorkspace를 생성한다.
                    기존 작업공간이 존재할경우, 제거하고 생성한다.
                    생성시에 네트워크 연결까지 약 10초 정도가 소요되며, 
                    제거시에는 약 45s 이상이 소요된다.

                    생성 완료 시 publicId 기반 서브도메인(WebSocket) 접속 정보를 반환한다.
                    """
    )
    @LogMonitoring
    @PostMapping("lab/terminal/stage/{stage_id}")
    public ResponseEntity<VirtualWorkspaceConnectResponse> applyVirtualWorkspace(@AuthenticationPrincipal Player player,
                                                            @PathVariable(name = "stage_id") Long stageId) {
        ReentrantLock lock = lockManager.getLock(player.getId(), stageId);
        if (lock.tryLock()) {
            try {
                VirtualWorkspaceConnectResponse response = virtualWorkspaceApplicationService
                        .recreate(stageId, player);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(response);
            } finally {
                lockManager.unlock(player.getId(), stageId);
            }
        }

        // 락 걸린 동안 들어오는 요청은 무시
        LOGGER.info("[applyVirtualWorkspace] 작업 중 중복된 요청 발생. stage: {}", stageId);
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(null);

    }

    // 접속 가능한 주소 조회
    @Operation(
            summary = "가상 작업공간 (VirtualWorkspace) 접속 주소 & 쿼리파라미터 조회",
            description = """
                    :stage에 대한 VirtualWorkspace 접속 주소 정보를 조회한다.
                    """
    )
    @GetMapping("lab/terminal/access-url/{stage_id}")
    @LogMonitoring
    public ResponseEntity<VirtualWorkspaceConnectResponse> getVirtualWorkspaceAccessUrl(@AuthenticationPrincipal Player player,
                                                                   @PathVariable(name = "stage_id") Long stageId) {
        VirtualWorkspaceConnectResponse response = virtualWorkspaceApplicationService.getAccessUrl(stageId, player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    // 현재 터미널 존재 여부 조회
    @Operation(
            summary = "기존 가상 작업공간 (VirtualWorkspace) 존재여부 조회",
            description = """
                    :stage에 대한 기존 VirtualWorkspace가 존재하는지를 확인한다.
                    """
    )
    @GetMapping("/lab/terminal/existence/{stage_id}")
    public ResponseEntity<VirtualWorkspaceExistenceResponse> checkVirtualWorkspaceExistence(@AuthenticationPrincipal Player player,
                                                                     @PathVariable(name = "stage_id") Long stageId) {
        VirtualWorkspaceExistenceResponse response = virtualWorkspaceApplicationService.checkExistence(stageId, player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
