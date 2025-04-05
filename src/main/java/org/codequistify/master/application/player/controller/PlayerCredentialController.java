package org.codequistify.master.application.player.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.player.dto.UpdatePasswordRequest;
import org.codequistify.master.application.player.service.PlayerCredentialService;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Player")
@RequestMapping("api/players")
public class PlayerCredentialController {

    private final Logger logger = LoggerFactory.getLogger(PlayerCredentialController.class);
    private final PlayerCredentialService playerCredentialService;

    @Operation(
            summary = "비밀번호 재설정",
            description = "비밀번호를 재설정합니다. 기존 비밀번호 인증이 필요합니다. password에 새로운 비밀번호를 담아 보냅니다.")
    @LogMonitoring
    @PatchMapping("password")
    public ResponseEntity<BasicResponse> updatePassword(@AuthenticationPrincipal Player player,
                                                        @RequestBody UpdatePasswordRequest request) {
        playerCredentialService.updatePassword(player, request);
        logger.info("[updatePassword] 비밀번호 재설정 완료, Player: {}", player.getUid());
        return ResponseEntity.ok(BasicResponse.of("SUCCESS"));
    }

    @Operation(
            summary = "비밀번호 초기화",
            description = "비밀번호를 초기화합니다. password에 새로운 비밀번호를 담아 보냅니다.")
    @LogMonitoring
    @PutMapping("password")
    public ResponseEntity<BasicResponse> resetPassword(@AuthenticationPrincipal Player player,
                                                       @RequestBody UpdatePasswordRequest request) {
        playerCredentialService.resetPassword(player, request);
        logger.info("[resetPassword] 비밀번호 초기화 완료, Player: {}", player.getUid());
        return ResponseEntity.ok(BasicResponse.of("SUCCESS"));
    }

    @Operation(
            summary = "계정 삭제",
            description = "계정을 삭제합니다. 즉시 해당 계정에 대한 데이터가 소실됩니다. 복구는 불가능합니다.")
    @LogMonitoring
    @DeleteMapping("{uid}")
    public ResponseEntity<BasicResponse> deletePlayer(@AuthenticationPrincipal Player player,
                                                      @PathVariable String uid) {
        if (!player.getUid().getValue().equals(uid)) {
            throw new ApplicationException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        playerCredentialService.deletePlayer(player);
        logger.info("[deletePlayer] 계정 삭제 완료, Player: {}", player.getUid());
        return ResponseEntity.ok(BasicResponse.of("SUCCESS"));
    }
}
