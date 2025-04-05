package org.codequistify.master.application.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.player.port.PlayerReader;
import org.codequistify.master.infrastructure.player.converter.PlayerConverter;
import org.codequistify.master.infrastructure.player.repository.PlayerJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerQueryService implements PlayerReader {

    private static final Logger logger = LoggerFactory.getLogger(PlayerQueryService.class);

    private final PlayerJpaRepository playerJpaRepository;

    /**
     * uid 기반 단일 조회
     */
    @Transactional(readOnly = true)
    public Player findOneByUid(PolId uid) {
        return playerJpaRepository.findByUid(uid.getValue())
                                  .map(PlayerConverter::toDomain)
                                  .orElseThrow(() -> {
                                   logger.warn("[findOneByUid] 존재하지 않는 player. uid={}", uid);
                                   return new ApplicationException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
                               });
    }

    /**
     * email 기반 단일 조회
     */
    @Transactional(readOnly = true)
    public Player findOneByEmail(String email) {
        return playerJpaRepository.findByEmail(email)
                                  .map(PlayerConverter::toDomain)
                                  .orElseThrow(() -> {
                                   logger.warn("[findOneByEmail] 존재하지 않는 email. email={}", email);
                                   return new ApplicationException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
                               });
    }

    /**
     * OAuthType 조회
     */
    @Transactional(readOnly = true)
    public OAuthType findOAuthTypeByEmail(String email) {
        return playerJpaRepository.getOAuthTypeByEmail(email)
                                  .orElseThrow(() -> {
                    logger.warn("[findOAtuhTypebyEmail] 존재하지 않는 email, email={}", email);
                    return new ApplicationException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
                });
    }

    /**
     * 이름 중복 여부 조회
     */
    @Transactional(readOnly = true)
    public boolean isDuplicatedName(String name) {
        return playerJpaRepository.existsByNameIgnoreCase(name);
    }

    /**
     * 이메일 존재 여부 조회
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return playerJpaRepository.findByEmail(email).isPresent();
    }
}