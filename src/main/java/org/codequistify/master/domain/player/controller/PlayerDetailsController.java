package org.codequistify.master.domain.player.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.details.PlayerInfoResponse;
import org.codequistify.master.domain.player.dto.details.ResetPasswordRequest;
import org.codequistify.master.domain.player.dto.details.UpdateDetailsRequest;
import org.codequistify.master.domain.player.service.impl.PlayerDetailsService;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Player")
@RequestMapping("api/players")
public class PlayerDetailsController {
    private final PlayerDetailsService playerDetailsService;

    private final Logger LOGGER = LoggerFactory.getLogger(PlayerDetailsController.class);

    @PutMapping("player/{id}/password")
    public ResponseEntity<BasicResponse> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        if (!request.id().equals(id)) {
            LOGGER.info("[resetPassword] 요청 id가 일치하지 않습니다 {} : {}", id, request.id());
            throw new IllegalArgumentException("요청된 id가 일치하지 않습니다");
        }

        playerDetailsService.resetPassword(request);
        return new ResponseEntity<>(
                new BasicResponse("비밀번호가 재설정 되었습니다.", null), HttpStatus.OK);
    }

    @PutMapping("player/{id}/level")
    public ResponseEntity<PlayerInfoResponse> addLevelPoint(@PathVariable Long id, @RequestBody UpdateDetailsRequest request) {
        if (!request.id().equals(id)) {
            LOGGER.info("[addLevelPoint] 요청 id가 일치하지 않습니다 {} : {}", id, request.id());
            throw new IllegalArgumentException("요청된 id가 일치하지 않습니다");
        }

        PlayerInfoResponse response = playerDetailsService.addLevelPoint(request).toPlayerInfoResponse();
        LOGGER.info("[] {}의 level point 증가", request.id());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
