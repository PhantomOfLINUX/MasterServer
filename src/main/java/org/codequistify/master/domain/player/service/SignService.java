package org.codequistify.master.domain.player.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.sign.LogInRequest;
import org.codequistify.master.domain.player.dto.sign.LogInResponse;
import org.codequistify.master.domain.player.dto.sign.SignUpRequest;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.global.exception.common.BusinessException;
import org.codequistify.master.global.exception.common.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SignService {
    private final PlayerRepository playerRepository;

    private final PlayerConverter playerConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(SignService.class);

    @Transactional
    public LogInResponse signUp(SignUpRequest request) {
        if (playerRepository.findByEmail(request.email()).isPresent()) {
            LOGGER.info("[signUp] 이미 존재하는 email 입니다.");
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        Player player = playerConverter.convert(request);

        player.encodePassword();
        player = playerRepository.save(player);

        LOGGER.info("[signUp] Player: {}, 회원가입 완료", player.getUid());

        return playerConverter.convert(player);
    }

    @Transactional
    public LogInResponse logIn(LogInRequest request) {
        Player player = playerRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    LOGGER.info("[logIn] 존재하지 않는 email 입니다.");
                    return new BusinessException(ErrorCode.INVALID_EMAIL_OR_PASSWORD, HttpStatus.BAD_REQUEST);
                });

        if (player.decodePassword(request.password())) {
            return playerConverter.convert(player);
        } else {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_OR_PASSWORD, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void logOut(Player player) {
        if (player.getOAuthAccessToken() != null && !player.getOAuthAccessToken().isBlank()) {
            revokeTokenForGoogle(player.getOAuthAccessToken());
            player.clearOAuthAccessToken();
        }

        player.clearRefreshToken();

        playerRepository.save(player);
    }

    private void revokeTokenForGoogle(String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String url = "https://accounts.google.com/o/oauth2/revoke?token=" + token;

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        } catch (RuntimeException exception) {
            LOGGER.info("[revokeTokenForGoogle] 만료시킬 수 없는 토큰");
        }
    }

    public boolean checkEmailDuplication(String email) {
        return playerRepository.findByEmail(email).isPresent();
    }

    public void updateRefreshToken(String uid, String refreshToken) {
        LOGGER.info("[updateRefreshToken] {}", uid);
        playerRepository.updateRefreshToken(uid, refreshToken);
    }
}
