package org.codequistify.master.application.player.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.player.dto.PlayerProfile;
import org.codequistify.master.application.player.dto.PlayerStageProgressResponse;
import org.codequistify.master.application.player.service.PlayerProfileService;
import org.codequistify.master.application.player.service.PlayerQueryService;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.stage.domain.CompletedStatus;
import org.codequistify.master.core.domain.stage.dto.HeatMapDataPoint;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.util.BasicResponse;
import org.codequistify.master.infrastructure.security.TokenPlayer;
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
@RequestMapping("/api/players")
@Tag(name = "Player")
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;
    private final PlayerQueryService playerQueryService;

    @LogMonitoring
    @GetMapping("")
    public ResponseEntity<List<PlayerProfile>> getAllPlayerProfiles() {
        List<PlayerProfile> response = playerProfileService.findAllPlayerProfiles();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "자신의 profile 조회",
            description = "현재 로그인된 사용자의 프로필 정보를 조회합니다."
    )
    @LogMonitoring
    @GetMapping("me/profile")
    public ResponseEntity<PlayerProfile> getMyProfile(@AuthenticationPrincipal TokenPlayer tokenPlayer) {
        Player player = playerQueryService.findOneByUid(tokenPlayer.getUid());
        return ResponseEntity.ok(PlayerProfile.from(player));
    }

    @Operation(
            summary = "자신의 문제풀이 상태를 조회",
            description = """
                    자신의 문제풀이 상태를 조회할 수 있다.
                    NOT_COMPLETED는 허용하지 않는다.
                    URI 정보는 현재 제공되지 않는다.
                    """
    )
    @LogMonitoring
    @GetMapping("me/stages")
    public ResponseEntity<PlayerStageProgressResponse> getStagesByStatusForPlayer(
            @AuthenticationPrincipal TokenPlayer tokenPlayer,
            @RequestParam CompletedStatus status
    ) {
        if (status == CompletedStatus.NOT_COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_SEARCH_CRITERIA, HttpStatus.BAD_REQUEST);
        }

        Player player = playerQueryService.findOneByUid(tokenPlayer.getUid());
        PlayerStageProgressResponse response = switch (status) {
            case COMPLETED -> playerProfileService.getCompletedStagesByPlayerId(player);
            case IN_PROGRESS -> playerProfileService.getInProgressStagesByPlayerId(player);
            default -> new PlayerStageProgressResponse(new ArrayList<>());
        };

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "자신의 문제풀이 히트맵 조회",
            description = "자신의 문제풀이 내역을 기반으로 히트맵 데이터를 반환합니다."
    )
    @LogMonitoring
    @GetMapping("me/heat-map")
    public ResponseEntity<List<HeatMapDataPoint>> getHeatMapPointsForPlayer(
            @AuthenticationPrincipal TokenPlayer tokenPlayer
    ) {
        Player player = playerQueryService.findOneByUid(tokenPlayer.getUid());
        List<HeatMapDataPoint> response = playerProfileService.getHeatMapDataPointsByModifiedDate(player);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "관리자 권한 확인",
            description = "현재 로그인된 사용자가 ADMIN 또는 SUPER_ADMIN 권한을 가지고 있는지 확인합니다."
    )
    @LogMonitoring
    @GetMapping("me/admin")
    public ResponseEntity<BasicResponse> checkAdminRole(@AuthenticationPrincipal TokenPlayer tokenPlayer) {
        Player player = playerQueryService.findOneByUid(tokenPlayer.getUid());
        boolean isAdmin = playerProfileService.isAdmin(player);
        return ResponseEntity.ok(BasicResponse.of(String.valueOf(isAdmin)));
    }
}