package org.codequistify.master.domain.player.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.details.ResetPasswordRequest;
import org.codequistify.master.domain.player.service.impl.PlayerDetailsService;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Player")
@RequestMapping("api/player")
public class PlayerDetailsController {
    private final PlayerDetailsService playerDetailsService;

    private final Logger LOGGER = LoggerFactory.getLogger(PlayerDetailsController.class);

    @PutMapping("password")
    public ResponseEntity<BasicResponse> resetPassword(@AuthenticationPrincipal Player player,
                                                       @RequestBody ResetPasswordRequest request) {
        playerDetailsService.resetPassword(player, request);
        LOGGER.info("[resetPassword] uid: {}, 비밀번호 재설정 완료", player.getUid());
        return new ResponseEntity<>(
                new BasicResponse("비밀번호가 재설정 되었습니다.", null), HttpStatus.OK);
    }
}
