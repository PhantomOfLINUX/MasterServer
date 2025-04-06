package org.codequistify.master.application.account.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.strategy.SignupPolicy;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.player.service.PlayerQueryService;
import org.codequistify.master.core.domain.player.model.HashedPassword;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.player.service.PlayerPasswordManager;
import org.codequistify.master.core.domain.vo.Email;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.jwt.dto.TokenInfo;
import org.codequistify.master.global.jwt.dto.TokenRequest;
import org.codequistify.master.global.jwt.dto.TokenResponse;
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

    private static final boolean DEFAULT_LOCKED = false;
    private static final String GOOGLE_REVOKE_URL = "https://accounts.google.com/o/oauth2/revoke?token=";
    private static final int INITIAL_EXP = 0;
    private static final String INITIAL_OAUTH_ID = "0";
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final PlayerPasswordManager playerPasswordManager;
    private final PlayerQueryService playerQueryService;
    private final PlayerRepository playerRepository;
    private final SignupPolicy signupPolicy;
    private final TokenProvider tokenProvider;

    @Transactional
    @LogMonitoring
    public Player signUp(String name, Email email, String rawPassword) {
        signupPolicy.validate(name, email, rawPassword);

        return Optional.of(rawPassword)
                       .map(playerPasswordManager::encode)
                       .map(HashedPassword::fromHashed)
                       .map(password -> Player.builder()
                                              .email(email)
                                              .name(name)
                                              .password(password)
                                              .oAuthType(OAuthType.POL)
                                              .oAuthId(INITIAL_OAUTH_ID)
                                              .locked(DEFAULT_LOCKED)
                                              .exp(INITIAL_EXP)
                                              .build())
                       .map(playerRepository::save)
                       .map(saved -> {
                           logger.info("[signUp] Player: {}, 회원가입 완료", saved.getUid());
                           return saved;
                       })
                       .orElseThrow(() -> new ApplicationException(ErrorCode.FAIL_PROCEED,
                                                                   HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Transactional
    @LogMonitoring
    public Player logIn(Email email, String password) {
        return Optional.ofNullable(playerQueryService.findOneByEmail(email))
                       .filter(p -> playerPasswordManager.matches(p, password))
                       .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_EMAIL_OR_PASSWORD,
                                                                   HttpStatus.BAD_REQUEST));
    }

    @Transactional
    @LogMonitoring
    public void logOut(Player player) {
        if (player.getOAuthAccessToken() != null && !player.getOAuthAccessToken().isBlank()) {
            revokeTokenForGoogle(player.getOAuthAccessToken());
            player = player.clearOAuthAccessToken();
        }

        player = player.clearRefreshToken();
        playerRepository.save(player);
    }

    private void revokeTokenForGoogle(String token) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String url = GOOGLE_REVOKE_URL + token;
            restTemplate.postForEntity(url, new HttpEntity<>(headers), String.class);
        } catch (RuntimeException e) {
            logger.info("[revokeTokenForGoogle] 만료시킬 수 없는 토큰");
        }
    }

    @LogMonitoring
    public boolean checkEmailDuplication(Email email) {
        return playerRepository.existsByEmailIgnoreCase(email);
    }

    @LogMonitoring
    public void updateRefreshToken(PolId uid, String refreshToken) {
        logger.info("[updateRefreshToken] {}", uid);
        playerRepository.updateRefreshToken(uid, refreshToken);
    }

    @LogMonitoring
    public TokenResponse regenerateAccessToken(TokenRequest request) {
        String uid = tokenProvider.getAudience(request.refreshToken());
        Player player = Optional.of(uid)
                                .map(PolId::of)
                                .map(playerQueryService::findOneByUid)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_TOKEN,
                                                                            HttpStatus.UNAUTHORIZED));

        String accessToken = player.getRefreshToken().equals(request.refreshToken())
                ? tokenProvider.generateAccessToken(player)
                : "";

        return new TokenResponse(request.refreshToken(), accessToken);
    }

    @LogExecutionTime
    public TokenInfo analyzeTokenInfo(String token) {
        Claims claims = Optional.ofNullable(tokenProvider.getClaims(token))
                                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_TOKEN,
                                                                            HttpStatus.BAD_REQUEST));

        SimpleDateFormat fmt = new SimpleDateFormat(TIMESTAMP_PATTERN);

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
