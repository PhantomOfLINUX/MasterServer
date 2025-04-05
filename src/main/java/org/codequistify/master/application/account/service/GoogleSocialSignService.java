package org.codequistify.master.application.account.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.vo.OAuthProfile;
import org.codequistify.master.application.account.vo.OAuthResource;
import org.codequistify.master.application.account.vo.OAuthToken;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.global.aspect.LogMethodInvocation;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.infrastructure.account.config.OAuthKey;
import org.codequistify.master.infrastructure.player.repository.PlayerRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service("google")
@RequiredArgsConstructor
public class GoogleSocialSignService implements SocialSignService {

    private static final String LOGIN_URL_TEMPLATE =
            "https://accounts.google.com/o/oauth2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=email%%20profile";

    private final RestTemplate restTemplate;
    private final OAuthKey oAuthKey;
    private final PlayerRepository playerRepository;

    @Override
    public String getSocialLogInURL() {
        return LOGIN_URL_TEMPLATE.formatted(
                oAuthKey.getGOOGLE_CLIENT_ID(),
                URLEncoder.encode(oAuthKey.getGOOGLE_REDIRECT_URI(), StandardCharsets.UTF_8)
        );
    }

    @LogMethodInvocation
    public OAuthProfile getOAuthProfile(String code) {
        return Optional.of(code)
                       .map(this::requestOAuthToken)
                       .map(token -> OAuthProfile.of(token, requestUserResource(token.access_token())))
                       .orElseThrow(() -> new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                                   HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    @LogMonitoring
    @Transactional
    public Player socialLogIn(OAuthProfile oAuthProfile) {
        return Optional.of(oAuthProfile.resource().email())
                       .flatMap(playerRepository::findByEmail)
                       .map(player -> player.withOAuthAccessToken(oAuthProfile.token().access_token()))
                       .map(playerRepository::save)
                       .orElseThrow(() -> new ApplicationException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    @LogMonitoring
    @Transactional
    public Player socialSignUp(OAuthProfile oAuthProfile) {
        Player player = Player.builder()
                              .name(oAuthProfile.resource().name())
                              .email(oAuthProfile.resource().email())
                              .oAuthType(OAuthType.GOOGLE)
                              .oAuthId(oAuthProfile.resource().id())
                              .locked(false)
                              .exp(0)
                              .build();

        return playerRepository.save(player);
    }

    private OAuthToken requestOAuthToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getGOOGLE_CLIENT_ID());
        body.add("client_secret", oAuthKey.getGOOGLE_CLIENT_SECRET());
        body.add("redirect_uri", oAuthKey.getGOOGLE_REDIRECT_URI());
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(oAuthKey.getGOOGLE_TOKEN_URI(), entity, OAuthToken.class);
        } catch (RestClientException e) {
            throw new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private OAuthResource requestUserResource(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return Optional.of(entity)
                       .map(e -> {
                           try {
                               return restTemplate.exchange(
                                       oAuthKey.getGOOGLE_RESOURCE_URI(),
                                       HttpMethod.GET,
                                       e,
                                       OAuthResource.class
                               ).getBody();
                           } catch (RestClientException | NullPointerException e1) {
                               throw new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                              HttpStatus.INTERNAL_SERVER_ERROR);
                           }
                       })
                       .orElseThrow(() -> new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                                   HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
