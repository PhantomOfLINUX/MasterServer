package org.codequistify.master.domain.lab.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.lab.dto.PtyUrlResponse;
import org.codequistify.master.domain.lab.service.LabService;
import org.codequistify.master.domain.player.domain.Player;
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

    private final String WS_HOST = "ws://ec2-13-125-76-129.ap-northeast-2.compute.amazonaws.com";

    @Operation(
            summary = "가상 터미널 연결 주소 발급",
            description = "가상 터미널 연결 주소를 발급한다. stage 별로 하나의 port를 사용해야만 한다.\n\n" +
                    "*주의사항* 실습이 종료되기 전에 연결 주소가 변경되면 이전 작업이 초기화 된다."
    )
    @LogMonitoring
    @GetMapping("lab/pty/stage/{stage_id}")
    public ResponseEntity<PtyUrlResponse> getPtyConnectionURL(@AuthenticationPrincipal Player player,
                                                              @PathVariable(name = "stage_id") Long stageId) {
        //Integer nodePort = labService.createStageOnKubernetes(stageId);
        PtyUrlResponse response = PtyUrlResponse.of(WS_HOST+":5050");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
