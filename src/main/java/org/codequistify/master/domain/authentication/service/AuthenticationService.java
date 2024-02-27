package org.codequistify.master.domain.authentication.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.dto.LogInRequest;
import org.codequistify.master.domain.authentication.dto.SignUpRequest;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.service.PlayerDetailsService;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.jwt.dto.TokenInfo;
import org.codequistify.master.global.jwt.dto.TokenRequest;
import org.codequistify.master.global.jwt.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final PlayerDetailsService playerDetailsService;

    private final PlayerConverter playerConverter;
    private final TokenProvider tokenProvider;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    @Transactional
    public PlayerProfile signUp(SignUpRequest request) {
        playerDetailsService.checkOAuthType(request.email())
                .ifPresent(authType -> {
                    if (authType.equals(OAuthType.POL)) {
                        LOGGER.info("[signUp] {} Email: {}", ErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), request.email());
                        throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
                    }
                    LOGGER.info("[signUp] {} Email: {}", ErrorCode.EMAIL_ALREADY_EXISTS_OTHER_AUTH.getMessage(), request.email());
                    throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS_OTHER_AUTH, HttpStatus.BAD_REQUEST, authType.name());
                });

        Player player = playerConverter.convert(request);

        player.encodePassword();
        player = playerDetailsService.save(player);

        LOGGER.info("[signUp] Player: {}, 회원가입 완료", player.getUid());

        return playerConverter.convert(player);
    }

    @Transactional
    public PlayerProfile logIn(LogInRequest request) {
        Player player;
        try {
            player = playerDetailsService.findOndPlayerByEmail(request.email());
        } catch (BusinessException exception) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_OR_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        if (!player.decodePassword(request.password())) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_OR_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        return playerConverter.convert(player);
    }

    @Transactional
    public void logOut(Player player) {
        if (player.getOAuthAccessToken() != null && !player.getOAuthAccessToken().isBlank()) {
            revokeTokenForGoogle(player.getOAuthAccessToken());
            player.clearOAuthAccessToken();
        }

        player.clearRefreshToken();

        playerDetailsService.save(player);
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
        return playerDetailsService.isExistPlayer(email);
    }

    public void updateRefreshToken(String uid, String refreshToken) {
        LOGGER.info("[updateRefreshToken] {}", uid);
        playerDetailsService.updateRefreshToken(uid, refreshToken);
    }

    public TokenResponse regenerateAccessToken(TokenRequest request) {
        String uid = tokenProvider.getAudience(request.refreshToken());
        Player player;
        try {
            player = playerDetailsService.findOndPlayerByUid(uid);
        } catch (BusinessException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
        }

        String accessToken = "";
        if (player.getRefreshToken().equals(request.refreshToken())) {
            accessToken = tokenProvider.generateAccessToken(player);
        }

        return new TokenResponse(request.refreshToken(), accessToken);
    }

    public TokenInfo analyzeTokenInfo(String token) {
        Claims claims = tokenProvider.getClaims(token);
        if (claims == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, HttpStatus.BAD_REQUEST);
        }

        String aud = claims.getAudience();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String iat = dateFormat.format(new Date(claims.getIssuedAt().getTime()));
        String exp = dateFormat.format(new Date(claims.getExpiration().getTime()));

        String iss = claims.getIssuer();
        List<String> roles = (List<String>) claims.get("role");
        boolean valid = tokenProvider.isValidatedToken(token);

        return new TokenInfo(aud, roles, iat, exp, iss, valid);
    }
}
