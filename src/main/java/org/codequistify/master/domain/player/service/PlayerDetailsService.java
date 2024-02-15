package org.codequistify.master.domain.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.ResetPasswordRequest;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.global.exception.common.BusinessException;
import org.codequistify.master.global.exception.common.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerDetailsService implements UserDetailsService {
    private final PlayerRepository playerRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(PlayerDetailsService.class);

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.info("[loadUserByUsername] loadUserByUsername: {}", username);
        return playerRepository.getPlayerByUid(username);
    }

    @Transactional
    public void resetPassword(Player player, ResetPasswordRequest request) {
        player.encodePassword(request.newPassword());
        playerRepository.save(player);

        LOGGER.info("[resetPassword] Player: {}, 비밀번호 재설정 성공", player.getUid());
    }

    @Transactional
    public Player save(Player player) {
        return playerRepository.save(player);
    }

    @Transactional
    public boolean isExistPlayer(String email) {
        return playerRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public Player findOndPlayerByEmail(String email) {
        return playerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.info("[findOndPlayerByEmail] 존재하지 않는 email 입니다. email: {}", email);
                    return new BusinessException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.BAD_REQUEST);
                });
    }

    @Transactional
    public Player findOndPlayerByUid(String uid) {
        return playerRepository.findByUid(uid)
                .orElseThrow(() -> {
                    LOGGER.info("[findOndPlayerByUid] 존재하지 않는 player 입니다. uid: {}", uid);
                    return new BusinessException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.BAD_REQUEST);
                });
    }

    @Transactional
    public void updateRefreshToken(String uid, String refreshToken) {
        playerRepository.updateRefreshToken(uid, refreshToken);
    }


}
