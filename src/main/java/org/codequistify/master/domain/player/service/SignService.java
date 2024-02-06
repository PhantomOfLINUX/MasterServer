package org.codequistify.master.domain.player.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.sign.LogInResponse;
import org.codequistify.master.domain.player.dto.sign.LogOutRequest;
import org.codequistify.master.domain.player.dto.sign.SignRequest;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public LogInResponse signUp(SignRequest request) {
        if (playerRepository.findByEmail(request.email()).isPresent()) {
            LOGGER.info("[signUp] 이미 존재하는 email 입니다.");
            throw new EntityExistsException("이미 존재하는 email입니다.");
        }

        Player player = playerConverter.convert(request);

        player.encodePassword();
        playerRepository.save(player);

        LOGGER.info("[signIn] {} player 회원가입 완료", player.getId());

        return playerConverter.convert(player);
    }

    @Transactional
    public LogInResponse signIn(SignRequest request) {
        Player player = playerRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    LOGGER.info("[signIn] 존재하지 않는 email 입니다.");
                    return new IllegalArgumentException("email 또는 password가 잘못되었습니다");
                });

        if (player.decodePassword(request.password())) {
            return playerConverter.convert(player);
        } else {
            throw new IllegalArgumentException("email 또는 password가 잘못되었습니다");
        }
    }

    @Transactional
    public void LogOut(LogOutRequest request, String token) {
        Player player = playerRepository.findByUid(request.uid())
                .orElseThrow(() -> {
                    LOGGER.info("[LogOut] 존재하지 않는 id 입니다.");
                    return new EntityNotFoundException("존재하지 않는 id 입니다");
                });

        if (!player.getOAuthAccessToken().isBlank()) {
            revokeTokenForGoogle(player.getOAuthAccessToken());
            player.clearOAuthAccessToken();
        }

        player.clearRefreshToken();

        playerRepository.save(player);
        LOGGER.info("{LogOut] {} 로그아웃", request.uid());
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
