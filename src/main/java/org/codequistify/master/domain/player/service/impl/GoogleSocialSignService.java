package org.codequistify.master.domain.player.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.sign.LogInResponse;
import org.codequistify.master.domain.player.dto.sign.OAuthResourceResponse;
import org.codequistify.master.domain.player.dto.sign.OAuthTokenResponse;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.domain.player.service.SocialSignService;
import org.codequistify.master.global.config.OAuthKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleSocialSignService implements SocialSignService {
    private final PlayerRepository playerRepository;
    private final RestTemplate restTemplate;

    private final PlayerConverter playerConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(GoogleSocialSignService.class);
    private final OAuthKey oAuthKey;

    /*
    구글 소셜 로그인 주소 반환
     */
    @Override
    public String getSocialSignInURL(){
        return "https://accounts.google.com/o/oauth2/auth?" +
                "client_id="+oAuthKey.getGOOGLE_CLIENT_ID() +
                "&redirect_uri="+oAuthKey.getGOOGLE_REDIRECT_URI() +
                "&response_type=code" +
                "&scope=email%20profile";
    }

    /*
    code를 통한 소셜 로그인
     */
    @Override
    @Transactional
    public LogInResponse socialLogin(String code) {
        String accessToken = getAccessToken(code);
        OAuthResourceResponse resource = getUserResource(accessToken);

        LOGGER.info("{} {} {}", resource.id(), resource.email(), resource.name());

        Optional<Player> playerOptional = playerRepository.findByEmail(resource.email());

        // 등록되지 않은 계정인 경우 player 등록하기
        Player player;
        if (playerOptional.isEmpty()) {
            LOGGER.info("등록되지 않은 구글 계정 {}", resource.email());
            player = playerRepository.save(
                    Player.builder()
                            .name(resource.name())
                            .email(resource.email())
                            .oAuthType(OAuthType.GOOGLE)
                            .oAuthId(resource.id()).build()
            );
            LOGGER.info("[socialLogin] 등록");
        } else {
            player = playerOptional.get();
        }

        player.updateOAuthAccessToken(accessToken);

        playerRepository.save(player);

        LogInResponse response = playerConverter.convert(player);
        LOGGER.info("[socialLogin] {} 구글 로그인", player.getEmail());

        return response;
    }

    private String getAccessToken(String code){
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
            OAuthTokenResponse response = restTemplate.postForObject(oAuthKey.getGOOGLE_TOKEN_URI(), entity, OAuthTokenResponse.class);
            return Objects.requireNonNull(response).access_token();
        } catch (RestClientException exception) {
            LOGGER.info("[getAccessToken] 토큰 요청 실패");
            return null;
        }
    }

    private OAuthResourceResponse getUserResource(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            OAuthResourceResponse response = restTemplate.exchange(oAuthKey.getGOOGLE_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceResponse.class).getBody();
            return response;
        } catch (RestClientException exception) {
            LOGGER.info("[getUserResource] 정보 요청 실패");
            return null;
        }
    }

}
