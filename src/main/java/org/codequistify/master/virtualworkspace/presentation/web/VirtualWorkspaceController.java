package org.codequistify.master.virtualworkspace.presentation.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.virtualworkspace.application.VirtualWorkspaceService;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceConnectionResponse;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceCreateRequest;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceStatusResponse;
import org.codequistify.master.virtualworkspace.presentation.dto.VirtualWorkspaceSummaryResponse;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequiredArgsConstructor
@Tag(name = "VirtualWorkspace")
@Validated
@RequestMapping("api/v1/virtualworkspaces")
public class VirtualWorkspaceController {
    private final VirtualWorkspaceService virtualWorkspaceService;
    private final LockManager lockManager;

    private final Logger LOGGER = LoggerFactory.getLogger(VirtualWorkspaceController.class);

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
    @PostMapping("/stages")
    public ResponseEntity<VirtualWorkspaceConnectionResponse> applyVirtualWorkspace(@AuthenticationPrincipal Player player,
                                                            @Valid @RequestBody VirtualWorkspaceCreateRequest request) {
        ReentrantLock lock = lockManager.getLock(player.getId(), request.stageCode());
        if (lock.tryLock()) {
            try {
                VirtualWorkspaceConnectionResponse response = virtualWorkspaceService
                        .recreate(request.stageCode(), player);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(response);
            } finally {
                lockManager.unlock(player.getId(), request.stageCode());
            }
        }

        // 락 걸린 동안 들어오는 요청은 무시
        LOGGER.info("[applyVirtualWorkspace] 작업 중 중복된 요청 발생. stageCode: {}", request.stageCode());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(null);

    }

    @Operation(
            summary = "가상 작업공간 (VirtualWorkspace) 조회",
            description = """
                    :stageCode에 대한 VirtualWorkspace 상태 정보를 조회한다.
                    """
    )
    @GetMapping("/stages/{stageCode}")
    @LogMonitoring
    public ResponseEntity<VirtualWorkspaceSummaryResponse> getVirtualWorkspace(@AuthenticationPrincipal Player player,
                                                                   @NotNull @PathVariable String stageCode) {
        VirtualWorkspaceSummaryResponse response = virtualWorkspaceService.getSummary(stageCode, player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "가상 작업공간 (VirtualWorkspace) 상태 조회",
            description = """
                    :stageCode에 대한 VirtualWorkspace 상태를 확인한다.
                    """
    )
    @GetMapping("/stages/{stageCode}/status")
    public ResponseEntity<VirtualWorkspaceStatusResponse> getVirtualWorkspaceStatus(@AuthenticationPrincipal Player player,
                                                                     @NotNull @PathVariable String stageCode) {
        VirtualWorkspaceStatusResponse response = virtualWorkspaceService.getStatus(stageCode, player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            summary = "가상 작업공간 (VirtualWorkspace) 연결 정보 조회",
            description = """
                    :stageCode에 대한 VirtualWorkspace 연결 정보를 조회한다.
                    """
    )
    @GetMapping("/stages/{stageCode}/connection")
    public ResponseEntity<VirtualWorkspaceConnectionResponse> getVirtualWorkspaceConnection(@AuthenticationPrincipal Player player,
                                                                     @NotNull @PathVariable String stageCode) {
        VirtualWorkspaceConnectionResponse response = virtualWorkspaceService.getConnection(stageCode, player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
