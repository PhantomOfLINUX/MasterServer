package org.codequistify.master.core.domain.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.player.domain.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.dto.UpdatePasswordRequest;
import org.codequistify.master.infrastructure.player.repository.PlayerRepository;
import org.codequistify.master.core.domain.stage.service.StageManagementService;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerDetailsService implements UserDetailsService {
    private final PlayerRepository playerRepository;
    private final StageManagementService stageManagementService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final Logger LOGGER = LoggerFactory.getLogger(PlayerDetailsService.class);

    @Override
    @Transactional
    @LogExecutionTime
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.info("[loadUserByUsername] loadUserByUsername: {}", username);
        return playerRepository.getPlayerByUid(username);
    }

    @Transactional
    @LogMonitoring
    public void resetPassword(Player player, UpdatePasswordRequest request) {
        player.encodePassword(request.password(), passwordEncoder);
        playerRepository.save(player);

        LOGGER.info("[resetPassword] Player: {}, 비밀번호 초기화 성공", player.getUid());
    }

    @Transactional
    @LogMonitoring
    public void updatePassword(Player player, UpdatePasswordRequest request) {
        // 비밀번호 데이터는 담기지 않으므로 다시 조회해야함
        player = playerRepository.getReferenceById(player.getId()); // jwt 필터에서 null 아닌 player 객체만 들어옴

        if (!player.decodePassword(request.rawPassword(), passwordEncoder)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_OR_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        player.encodePassword(request.password(), passwordEncoder);
        playerRepository.save(player);

        LOGGER.info("[updatePassword] Player: {}, 비밀번호 재설정 성공", player.getUid());
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
    public Optional<OAuthType> checkOAuthType(String email) {
        return playerRepository.getOAuthTypeByEmail(email);
    }

    @Transactional
    public Player findOnePlayerByEmail(String email) {
        return playerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOGGER.info("[findOndPlayerByEmail] 존재하지 않는 email 입니다. email: {}", email);
                    return new BusinessException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.BAD_REQUEST);
                });
    }

    @Transactional
    public Player findOnePlayerByUid(String uid) {
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

    @Transactional
    @LogMonitoring
    public void deletePlayer(Player player) {
        player.dataClear();
        playerRepository.save(player);
    }


}
