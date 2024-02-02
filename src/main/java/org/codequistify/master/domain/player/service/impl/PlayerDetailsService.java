package org.codequistify.master.domain.player.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.domain.player.dto.details.ResetPasswordRequest;
import org.codequistify.master.domain.player.dto.details.UpdateDetailsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerDetailsService {
    private final PlayerRepository playerRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(PlayerDetailsService.class);

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Player player = playerRepository.findById(request.id())
                .orElseThrow(() -> {
                    LOGGER.info("[resetPassword] 존재하지 않는 player {}", request.id());
                    return new EntityNotFoundException("존재하지 않는 player 입니다");
                });

        player.encodePassword(request.newPassword());

        playerRepository.save(player);
        LOGGER.info("[resetPassword] {} 비밀번호 재설정 성공", request.id());
    }

    @Transactional
    public Player addLevelPoint(UpdateDetailsRequest request) {
        Player player = playerRepository.findById(request.id())
                .orElseThrow(() -> {
                    LOGGER.info("[addLevelPoint] 존재하지 않는 player {}", request.id());
                    return new EntityNotFoundException("존재하지 않는 player 입니다");
                });

        player.increaseLevelPoint(request.levelPoint());
        player = playerRepository.save(player);

        LOGGER.info("[addLevelPoint] player {}, 레벨 증가 : {}", player.getId(), player.getLevel());

        return player;
    }
}
