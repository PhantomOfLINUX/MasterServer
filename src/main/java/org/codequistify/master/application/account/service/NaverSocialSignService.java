package org.codequistify.master.application.account.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.vo.OAuthProfile;
import org.codequistify.master.application.account.vo.OAuthResource;
import org.codequistify.master.application.account.vo.OAuthToken;
import org.codequistify.master.application.account.vo.ResourceOfNaver;
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

@Service("naver")
@RequiredArgsConstructor
public class NaverSocialSignService implements SocialSignService {

    private static final String LOGIN_URL_TEMPLATE =
            "https://nid.naver.com/oauth2.0/authorize?client_id=%s&redirect_uri=%s&response_type=code&state=%s";

    private final RestTemplate restTemplate;
    private final PlayerRepository playerRepository;
    private final OAuthKey oAuthKey;

    /**
     * 네이버 소셜 로그인 URL 반환
     */
    @Override
    public String getSocialLogInURL() {
        return LOGIN_URL_TEMPLATE.formatted(
                oAuthKey.getNAVER_CLIENT_ID(),
                URLEncoder.encode(oAuthKey.getNAVER_REDIRECT_URI(), StandardCharsets.UTF_8),
                URLEncoder.encode("pol", StandardCharsets.UTF_8)
        );
    }

    /**
     * 인가 코드를 기반으로 OAuth 토큰 + 리소스 획득
     */
    @LogMethodInvocation
    public OAuthProfile getOAuthProfile(String code) {
        return Optional.of(code)
                       .map(this::requestOAuthToken)
                       .map(token -> OAuthProfile.of(token, requestUserResource(token.access_token())))
                       .orElseThrow(() -> new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                                   HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * 네이버 소셜 로그인
     */
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

    /**
     * 네이버 소셜 회원가입
     */
    @Override
    @LogMonitoring
    @Transactional
    public Player socialSignUp(OAuthProfile oAuthProfile) {
        Player player = Player.builder()
                              .name(oAuthProfile.resource().name())
                              .email(oAuthProfile.resource().email())
                              .oAuthType(OAuthType.NAVER)
                              .oAuthId(oAuthProfile.resource().id())
                              .locked(false)
                              .exp(0)
                              .build();

        return playerRepository.save(player);
    }

    /**
     * 인가 코드를 통한 AccessToken 발급
     */
    private OAuthToken requestOAuthToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getNAVER_CLIENT_ID());
        body.add("client_secret", oAuthKey.getNAVER_CLIENT_SECRET());
        body.add("grant_type", "authorization_code");
        body.add("state", "pol");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(oAuthKey.getNAVER_TOKEN_URI(), entity, OAuthToken.class);
        } catch (RestClientException e) {
            throw new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * AccessToken으로 사용자 정보 조회
     */
    private OAuthResource requestUserResource(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return Optional.of(entity)
                       .map(e -> {
                           try {
                               return restTemplate.exchange(
                                       oAuthKey.getNAVER_RESOURCE_URI(),
                                       HttpMethod.GET,
                                       e,
                                       ResourceOfNaver.class
                               ).getBody();
                           } catch (RestClientException | NullPointerException ex) {
                               throw new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                              HttpStatus.INTERNAL_SERVER_ERROR);
                           }
                       })
                       .map(ResourceOfNaver::toOAuthResource)
                       .orElseThrow(() -> new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                                                   HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
