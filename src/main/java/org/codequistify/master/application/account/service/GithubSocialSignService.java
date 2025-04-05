package org.codequistify.master.application.account.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.vo.OAuthProfile;
import org.codequistify.master.application.account.vo.OAuthResource;
import org.codequistify.master.application.account.vo.OAuthToken;
import org.codequistify.master.application.account.vo.ResourceOfGithub;
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

@Service("github")
@RequiredArgsConstructor
public class GithubSocialSignService implements SocialSignService {

    private static final String LOGIN_URL_TEMPLATE =
            "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&state=%s";

    private final RestTemplate restTemplate;
    private final OAuthKey oAuthKey;
    private final PlayerRepository playerRepository;

    @Override
    public String getSocialLogInURL() {
        return LOGIN_URL_TEMPLATE.formatted(
                oAuthKey.getGITHUB_CLIENT_ID(),
                URLEncoder.encode(oAuthKey.getGITHUB_REDIRECT_URI(), StandardCharsets.UTF_8),
                URLEncoder.encode("pol", StandardCharsets.UTF_8)
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
                       .map(p -> p.withOAuthAccessToken(oAuthProfile.token().access_token()))
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
                              .oAuthType(OAuthType.GITHUB)
                              .oAuthId(oAuthProfile.resource().id())
                              .locked(false)
                              .exp(0)
                              .build();

        return playerRepository.save(player);
    }

    private OAuthToken requestOAuthToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getGITHUB_CLIENT_ID());
        body.add("client_secret", oAuthKey.getGITHUB_CLIENT_SECRET());
        body.add("grant_type", "authorization_code");
        body.add("state", "pol");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(oAuthKey.getGITHUB_TOKEN_URI(), entity, OAuthToken.class);
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
                                       oAuthKey.getGITHUB_RESOURCE_URI(),
                                       HttpMethod.GET,
                                       e,
                                       ResourceOfGithub.class
                               ).getBody();
                           } catch (RestClientException | NullPointerException e1) {
                               throw new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                              HttpStatus.INTERNAL_SERVER_ERROR);
                           }
                       })
                       .map(ResourceOfGithub::toOAuthResource)
                       .orElseThrow(() -> new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                                   HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
