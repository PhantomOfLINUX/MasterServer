package org.codequistify.master.application.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.player.dto.UpdatePasswordRequest;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.service.PlayerPasswordManager;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.infrastructure.player.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerCredentialService {

    private final PlayerRepository playerRepository;
    private final PlayerPasswordManager playerPasswordManager;

    private final Logger logger = LoggerFactory.getLogger(PlayerCredentialService.class);

    @Transactional
    @LogExecutionTime
    public void updatePassword(Player player, UpdatePasswordRequest request) {
        Player updated = playerRepository.findByUid(player.getUid())
                                         .map(current -> {
                                             if (!playerPasswordManager.matches(current, request.rawPassword())) {
                                                 throw new ApplicationException(ErrorCode.INVALID_EMAIL_OR_PASSWORD, HttpStatus.BAD_REQUEST);
                                             }
                                             return playerPasswordManager.encodePassword(current, request.password());
                                         })
                                         .orElseThrow(() -> new ApplicationException(ErrorCode.PLAYER_NOT_FOUND,
                                                                                     HttpStatus.NOT_FOUND));

        playerRepository.save(updated);
        logger.info("[updatePassword] Player: {}, 비밀번호 변경 성공", updated.getUid());
    }

    @Transactional
    @LogMonitoring
    public void resetPassword(Player player, UpdatePasswordRequest request) {
        Player updated = playerPasswordManager.encodePassword(player, request.password());
        playerRepository.save(updated);

        logger.info("[resetPassword] Player: {}, 비밀번호 초기화 성공", updated.getUid());
    }

    @Transactional
    public void deletePlayer(Player player) {
        Player deleted = player.dataClear();
        playerRepository.save(deleted);

        logger.info("[deletePlayer] Player: {}, 계정 삭제 처리 완료", deleted.getUid());
    }
}
