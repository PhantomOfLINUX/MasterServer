package org.codequistify.master.application.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.authentication.service.SocialSignService;
import org.codequistify.master.application.authentication.vo.OAuthData;
import org.codequistify.master.application.authentication.vo.OAuthResource;
import org.codequistify.master.application.authentication.vo.OAuthToken;
import org.codequistify.master.core.domain.player.converter.PlayerConverter;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.application.player.dto.PlayerProfile;
import org.codequistify.master.core.domain.player.service.PlayerDetailsService;
import org.codequistify.master.global.aspect.LogMethodInvocation;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.config.OAuthKey;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleSocialSignService implements SocialSignService {
    private final PlayerDetailsService playerDetailsService;
    private final PlayerConverter playerConverter;

    private final Logger LOGGER = LoggerFactory.getLogger(GoogleSocialSignService.class);
    private final RestTemplate restTemplate;
    private final OAuthKey oAuthKey;

    /*
    구글 소셜 로그인 주소 반환
     */
    @Override
    public String getSocialLogInURL() {
        return "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=" + oAuthKey.getGOOGLE_CLIENT_ID() +
                "&redirect_uri=" + oAuthKey.getGOOGLE_REDIRECT_URI() +
                "&response_type=code" +
                "&scope=email%20profile";
    }

    @LogMethodInvocation
    public OAuthData getOAuthData(String code) {
        OAuthToken token = getOAuthToken(code);
        OAuthResource resource = getUserResource(token.access_token());
        return OAuthData.of(token, resource);
    }

    /*
    code를 통한 소셜 로그인
     */
    @Override
    @LogMonitoring
    @Transactional
    public PlayerProfile socialLogIn(OAuthData oAuthData) {

        Player player = playerDetailsService.findOnePlayerByEmail(oAuthData.resource().email());

        player.updateOAuthAccessToken(oAuthData.token().access_token());

        playerDetailsService.save(player);

        PlayerProfile response = playerConverter.convert(player);
        LOGGER.info("[socialLogin] 구글 로그인, Player: {}", player.getUsername());

        return response;
    }

    @Override
    @LogMonitoring
    @Transactional
    public Player socialSignUp(OAuthData oAuthData) {
        Player player = Player.builder()
                .name(oAuthData.resource().name())
                .email(oAuthData.resource().email())
                .oAuthType(OAuthType.GOOGLE)
                .oAuthId(oAuthData.resource().id())
                .locked(false)
                .exp(0)
                .build();
        player = playerDetailsService.save(player);
        LOGGER.info("[socialSignUp] 신규 구글 사용자 등록, Player: {}", oAuthData.resource().email());
        return player;
    }

    @LogMethodInvocation
    private OAuthToken getOAuthToken(String code) {
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
            OAuthToken response = restTemplate.postForObject(oAuthKey.getGOOGLE_TOKEN_URI(), entity, OAuthToken.class);
            LOGGER.info("[getOAuthToken] token 정보{}", response);
            return response;
        } catch (RestClientException exception) {
            LOGGER.info("[getOAuthToken] 토큰 요청 실패");
            throw new BusinessException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LogMethodInvocation
    private OAuthResource getUserResource(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(oAuthKey.getGOOGLE_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResource.class).getBody();
        } catch (NullPointerException | RestClientException exception) {
            LOGGER.info("[getUserResource] 정보 요청 실패");
            throw new BusinessException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
