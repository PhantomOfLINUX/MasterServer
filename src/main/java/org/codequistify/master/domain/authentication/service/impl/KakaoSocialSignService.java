package org.codequistify.master.domain.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.dto.LogInResponse;
import org.codequistify.master.domain.authentication.service.SocialSignService;
import org.codequistify.master.domain.authentication.vo.OAuthResourceVO;
import org.codequistify.master.domain.authentication.vo.OAuthTokenVO;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.service.PlayerDetailsService;
import org.codequistify.master.global.config.OAuthKey;
import org.codequistify.master.global.exception.common.BusinessException;
import org.codequistify.master.global.exception.common.ErrorCode;
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
import java.util.Objects;

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


    /*
    code를 통한 소셜 로그인
     */
    @Override
    public LogInResponse socialLogIn(String code) {

        String accessToken = getAccessToken(code);
        OAuthResourceVO resource = getUserResource(accessToken);
        Map<String, String> properties = Objects.requireNonNull(resource).properties();
        LOGGER.info("{} {}", resource.id(), properties.get("nickname"));

        Player player;
        try {
            player = playerDetailsService.findOndPlayerByEmail(properties.get("email"));
        } catch (BusinessException exception) {
            LOGGER.info("등록되지 않은 카카오 계정 {}", properties.get("email"));
            player = socialSignUp(resource);
        }

        player.updateOAuthAccessToken(accessToken);

        playerDetailsService.save(player);

        LogInResponse response = playerConverter.convert(player);
        LOGGER.info("[socialLogin] {} 카카오 로그인", player.getEmail());

        return response;
    }

    @Override
    @Transactional
    public Player socialSignUp(OAuthResourceVO resource) {
        Map<String, String> properties = Objects.requireNonNull(resource).properties();
        Player player = Player.builder()
                .name(properties.get("nickname"))
                .email(properties.get("email"))
                .oAuthType(OAuthType.KAKAO)
                .oAuthId(resource.id()).build();
        player = playerDetailsService.save(player);
        LOGGER.info("[socialSignUp] 등록");
        return player;
    }

    private String getAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getKAKAO_CLIENT_ID());
        body.add("redirect_uri", oAuthKey.getKAKAO_REDIRECT_URI());
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            OAuthTokenVO response = restTemplate.postForObject(oAuthKey.getKAKAO_TOKEN_URI(), entity, OAuthTokenVO.class);
            return Objects.requireNonNull(response).access_token();
        } catch (RestClientException exception) {
            LOGGER.info("[getAccessToken] 토큰 요청 실패");
            throw new BusinessException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private OAuthResourceVO getUserResource(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(oAuthKey.getKAKAO_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceVO.class).getBody();
        } catch (NullPointerException | RestClientException exception) {
            LOGGER.info("[getUserResource] 정보 요청 실패");
            throw new BusinessException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
