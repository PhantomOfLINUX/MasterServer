package org.codequistify.master.core.domain.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.authentication.service.SocialSignService;
import org.codequistify.master.core.domain.authentication.vo.OAuthData;
import org.codequistify.master.core.domain.authentication.vo.OAuthResource;
import org.codequistify.master.core.domain.authentication.vo.OAuthToken;
import org.codequistify.master.core.domain.authentication.vo.ResourceOfKakao;
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

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoSocialSignService implements SocialSignService {
    private final PlayerDetailsService playerDetailsService;
    private final PlayerConverter playerConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(KakaoSocialSignService.class);
    private final RestTemplate restTemplate;
    private final OAuthKey oAuthKey;

    /*
    카카오 소셜 로그인 주소 반환
     */
    @Override
    public String getSocialLogInURL() {
        return "https://kauth.kakao.com/oauth/authorize?response_type=code" +
                "&client_id=" + oAuthKey.getKAKAO_CLIENT_ID() +
                "&redirect_uri=" + oAuthKey.getKAKAO_REDIRECT_URI();
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
        LOGGER.info("[socialLogin] 카카오 로그인, Player: {}", player.getUsername());

        return response;
    }

    @Override
    @LogMonitoring
    @Transactional
    public Player socialSignUp(OAuthData oAuthData) {
        Player player = Player.builder()
                .name(oAuthData.resource().name())
                .email(oAuthData.resource().email())
                .oAuthType(OAuthType.KAKAO)
                .oAuthId(oAuthData.resource().id())
                .locked(false)
                .exp(0)
                .build();
        player = playerDetailsService.save(player);
        LOGGER.info("[socialSignUp] 신규 카카오 사용자 등록, Player: {}", oAuthData.resource().email());
        return player;
    }

    @LogMethodInvocation
    private OAuthToken getOAuthToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getKAKAO_CLIENT_ID());
        body.add("redirect_uri", oAuthKey.getKAKAO_REDIRECT_URI());
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            OAuthToken response = restTemplate.postForObject(oAuthKey.getKAKAO_TOKEN_URI(), entity, OAuthToken.class);
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
            Map<String, String> map = restTemplate.exchange(oAuthKey.getKAKAO_RESOURCE_URI(), HttpMethod.GET, entity, ResourceOfKakao.class).getBody().properties();
            OAuthResource response = new OAuthResource(map.get("id"), map.get("email"), map.get("nickname"));
            LOGGER.info("[getUserResource] 리소스: {}", response);
            return response;
        } catch (NullPointerException | RestClientException exception) {
            LOGGER.info("[getUserResource] 정보 요청 실패");
            throw new BusinessException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
