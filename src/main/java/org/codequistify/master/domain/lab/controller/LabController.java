package org.codequistify.master.domain.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.dto.pShellCreateResponse;
import org.codequistify.master.domain.lab.service.LabService;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.service.impl.StageServiceImpl;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LabController {
    private final LabService labService;
    private final StageServiceImpl stageService;

    private final String DEPLOY_HOST = "ws://ec2-13-125-76-129.ap-northeast-2.compute.amazonaws.com";
    private final String LAB_HOST = "https://lab.pol.or.kr";

    @Operation(
            summary = "가상 터미널 생성요청",
            description = """
                    :stage에 대한 터미널을 생성한다.
                    
                    사용시에는 xHeaders 배열에 들어있는 헤더 값을 추가해서 connection 연결을 보내야한다.
                    """
    )
    @LogMonitoring
    @GetMapping("lab/terminal/stage/{stage_id}")
    public ResponseEntity<pShellCreateResponse> applyPShell(@AuthenticationPrincipal Player player,
                                                            @PathVariable(name = "stage_id") Long stageId) {
        Stage stage = stageService.findStageById(stageId);

        labService.deleteStageOnKubernetes(player, stage); // 기존에 워크로드가 존재할 경우 제거
        labService.createStageOnKubernetes(player, stage); // 신규 PShell 생성

        pShellCreateResponse response = pShellCreateResponse
                .of(LAB_HOST, player.getUid(), stage.getStageImage().name().toLowerCase());


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
