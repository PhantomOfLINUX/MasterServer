package org.codequistify.master.domain.player.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.service.PlayerService;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plyers")
@Tag(name = "Player")
public class PlayerController {
    private final PlayerService playerService;
    private final PlayerConverter playerConverter;

    @LogMonitoring
    @GetMapping("")
    public ResponseEntity<List<PlayerProfile>> getAllPlayerProfiles() {
        List<PlayerProfile> response =  playerService.findAllPlayerProfiles();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @LogMonitoring
    @Operation(
            summary = "자신의 profile 조회",
            description = "자신의 profile 정보를 응답받는다. 자신이란 현재 소유중인 엑세스 토큰에 기반한다."
    )
    @GetMapping("me")
    public ResponseEntity<PlayerProfile> getMyProfile(@AuthenticationPrincipal Player player) {
        PlayerProfile playerProfile = playerConverter.convert(player);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(playerProfile);
    }
}
