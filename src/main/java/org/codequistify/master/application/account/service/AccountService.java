package org.codequistify.master.application.account.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.player.service.PlayerProfileService;
import org.codequistify.master.application.player.service.PlayerQueryService;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.player.service.PlayerPasswordManager;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.jwt.dto.TokenInfo;
import org.codequistify.master.global.jwt.dto.TokenRequest;
import org.codequistify.master.global.jwt.dto.TokenResponse;
import org.codequistify.master.infrastructure.player.converter.PlayerConverter;
import org.codequistify.master.infrastructure.player.entity.PlayerEntity;
import org.codequistify.master.infrastructure.player.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final PlayerQueryService playerQueryService;
    private final PlayerProfileService playerProfileService;
    private final PlayerPasswordManager playerPasswordManager;
    private final PlayerValidator playerValidator;
    private final TokenProvider tokenProvider;
    private final PlayerRepository playerRepository;

    private final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Transactional
    @LogMonitoring
    public Player signUp(String name, String email, String password) {
        validateSignUp(name, email, password);

        Player newPlayer = Player.builder()
                                 .email(email)
                                 .name(name)
                                 .password(password)
                                 .oAuthType(OAuthType.POL)
                                 .oAuthId("0")
                                 .locked(false)
                                 .exp(0)
                                 .build();

        Player encoded = playerPasswordManager.encodePassword(newPlayer);
        PlayerEntity saved = playerRepository.save(PlayerConverter.toEntity(encoded));
        logger.info("[signUp] Player: {}, 회원가입 완료", encoded.getUid());

        return PlayerConverter.toDomain(saved);
    }

    private void validateSignUp(String name, String email, String password) {
        playerRepository.getOAuthTypeByEmail(email).ifPresent(authType -> {
            ErrorCode code = authType.equals(OAuthType.POL)
                    ? ErrorCode.EMAIL_ALREADY_EXISTS
                    : ErrorCode.EMAIL_ALREADY_EXISTS_OTHER_AUTH;
            throw new ApplicationException(code, HttpStatus.BAD_REQUEST, authType.name());
        });

        Optional.of(password)
                .filter(playerValidator::isValidPassword)
                .orElseThrow(() -> new BusinessException(ErrorCode.PASSWORD_POLICY_VIOLATION, HttpStatus.BAD_REQUEST));

        Optional.of(name)
                .filter(playerValidator::isValidName)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFANITY_IN_NAME, HttpStatus.BAD_REQUEST));

        if (playerProfileService.isDuplicatedName(name)) {
            throw new ApplicationException(ErrorCode.DUPLICATE_NAME, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @LogMonitoring
    public Player logIn(String email, String password) {
        Player player = Optional.ofNullable(playerQueryService.findOneByEmail(email))
                                .filter(p -> playerPasswordManager.matches(p, password))
                                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_EMAIL_OR_PASSWORD,
                                                                            HttpStatus.BAD_REQUEST));

        return player;
    }

    @Transactional
    @LogMonitoring
    public void logOut(Player player) {
        if (player.getOAuthAccessToken() != null && !player.getOAuthAccessToken().isBlank()) {
            revokeTokenForGoogle(player.getOAuthAccessToken());
            player = player.clearOAuthAccessToken();
        }

        player = player.clearRefreshToken();
        playerRepository.save(PlayerConverter.toEntity(player));
    }

    private void revokeTokenForGoogle(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String url = "https://accounts.google.com/o/oauth2/revoke?token=" + token;
            restTemplate.postForEntity(url, new HttpEntity<>(headers), String.class);
        } catch (RuntimeException e) {
            logger.info("[revokeTokenForGoogle] 만료시킬 수 없는 토큰");
        }
    }

    @LogMonitoring
    public boolean checkEmailDuplication(String email) {
        return playerRepository.existsByEmailIgnoreCase(email);
    }

    @LogMonitoring
    public void updateRefreshToken(String uid, String refreshToken) {
        logger.info("[updateRefreshToken] {}", uid);
        playerRepository.updateRefreshToken(uid, refreshToken);
    }

    @LogMonitoring
    public TokenResponse regenerateAccessToken(TokenRequest request) {
        String uid = tokenProvider.getAudience(request.refreshToken());
        Player player = Optional.of(uid)
                                .map(PolId::of)
                                .map(playerQueryService::findOneByUid)
                                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN,
                                                                         HttpStatus.UNAUTHORIZED));

        String accessToken = player.getRefreshToken().equals(request.refreshToken())
                ? tokenProvider.generateAccessToken(player)
                : "";

        return new TokenResponse(request.refreshToken(), accessToken);
    }

    @LogExecutionTime
    public TokenInfo analyzeTokenInfo(String token) {
        Claims claims = Optional.ofNullable(tokenProvider.getClaims(token))
                                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN,
                                                                         HttpStatus.BAD_REQUEST));

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return new TokenInfo(
                claims.getAudience(),
                (List<String>) claims.get("role"),
                fmt.format(claims.getIssuedAt()),
                fmt.format(claims.getExpiration()),
                claims.getIssuer(),
                tokenProvider.isValidatedToken(token)
        );
    }
}