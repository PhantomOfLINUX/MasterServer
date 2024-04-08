package org.codequistify.master.domain.player.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.domain.player.service.PlayerProfileService;
import org.codequistify.master.domain.stage.domain.CompletedStatus;
import org.codequistify.master.domain.stage.dto.HeatMapDataPoint;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plyers")
@Tag(name = "Player")
public class PlayerProfileController {
    private final PlayerProfileService playerProfileService;
    private final PlayerConverter playerConverter;

    @LogMonitoring
    @GetMapping("")
    public ResponseEntity<List<PlayerProfile>> getAllPlayerProfiles() {
        List<PlayerProfile> response =  playerProfileService.findAllPlayerProfiles();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @LogMonitoring
    @Operation(
            summary = "자신의 profile 조회",
            description = "자신의 profile 정보를 응답받는다. 자신이란 현재 소유중인 엑세스 토큰에 기반한다."
    )
    @GetMapping("me/profile")
    public ResponseEntity<PlayerProfile> getMyProfile(@AuthenticationPrincipal Player player) {
        PlayerProfile playerProfile = playerConverter.convert(player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(playerProfile);
    }

    @GetMapping("me/stages")
    @Operation(
            summary = "자신의 문제풀이 상태를 조회할 수 있다.",
            description = "자신의 문제풀이 상태를 조회할 수 있다. 자신이란 현재 소유중인 엑세스 토큰에 기반한다.\n\n" +
                    "*NOT_COMPLETED*는 허용하지 않는다." +
                    " *uri 정보는 현재 제공되지 않는다.*"
    )
    public ResponseEntity<PlayerStageProgressResponse> getStagesByStatusForPlayer(@AuthenticationPrincipal Player player,
                                                                                  @RequestParam CompletedStatus status) {
        if (status.equals(CompletedStatus.NOT_COMPLETED)) {
            throw new BusinessException(ErrorCode.INVALID_SEARCH_CRITERIA, HttpStatus.BAD_REQUEST);
        }

        PlayerStageProgressResponse response = new PlayerStageProgressResponse(new ArrayList<>());
        if (status.equals(CompletedStatus.COMPLETED)) {
            response = playerProfileService.getCompletedStagesByPlayerId(player);
        }
        if (status.equals(CompletedStatus.IN_PROGRESS)) {
            response = playerProfileService.getInProgressStagesByPlayerId(player);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("me/heat-map")
    public ResponseEntity<List<HeatMapDataPoint>> getHeatMapPointsForPlayer(@AuthenticationPrincipal Player player) {
        List<HeatMapDataPoint> response = playerProfileService.getHeatMapDataPointsByModifiedDate(player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
