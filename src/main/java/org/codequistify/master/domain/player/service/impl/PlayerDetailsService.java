package org.codequistify.master.domain.player.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.details.ResetPasswordRequest;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


}
