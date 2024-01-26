package org.codequistify.master.domain.player.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.domain.repository.PlayerRepository;
import org.codequistify.master.domain.player.dto.OAuthResourceResponse;
import org.codequistify.master.domain.player.dto.OAuthTokenResponse;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.dto.SignInResponse;
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
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleSocialSignService implements SocialSignService {
    private final SignService signService;
    private final PlayerRepository playerRepository;
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
    public SignInResponse socialLogin(String code) {
        String accessToken = getAccessToken(code);
        OAuthResourceResponse resource = getUserResource(accessToken);

        LOGGER.info("{} {} {}", resource.id(), resource.email(), resource.name());

        Optional<Player> playerOptional = playerRepository.findByEmail(resource.email());

        // 등록되지 않은 계정인 경우 player 등록하기
        if (playerOptional.isEmpty()) {
            LOGGER.info("등록되지 않은 구글 계정 {}", resource.email());
            signService.signUpBySocial(new PlayerDTO(
                    null,
                    resource.email(),
                    resource.name(),
                    "google",
                    resource.id(),
                    0)
            );

            playerOptional = playerRepository.findByEmail(resource.email());
        }

        Player player = playerOptional.get();
        player.updateOAuthAccessToken(accessToken);

        playerRepository.save(player);

        SignInResponse response = player.toSignInResponse();
        LOGGER.info("[socialLogin] {} 구글 로그인", player.getEmail());

        return response;
    }

    private String getAccessToken(String code){
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getGOOGLE_CLIENT_ID());
        body.add("client_secret", oAuthKey.getGOOGLE_CLIENT_SECRET());
        body.add("redirect_uri", oAuthKey.getGOOGLE_REDIRECT_URI());
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        OAuthTokenResponse response = restTemplate.postForObject(oAuthKey.getGOOGLE_TOKEN_URI(), entity, OAuthTokenResponse.class);

        if (response.access_token() != null){
            return response.access_token();
        }else {
            return null;
        }
    }

    private OAuthResourceResponse getUserResource(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        OAuthResourceResponse response = restTemplate.exchange(oAuthKey.getGOOGLE_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceResponse.class).getBody();

        if (response != null){
            return response;
        }else {
            return null;
        }
    }

    @Transactional
    public PlayerDTO TEST_socialLogin(String code) {
        LOGGER.info("[TEST_socialLogin]");
        String accessToken = TEST_getAccessToken(code);
        OAuthResourceResponse resource = TEST_getUserResource(accessToken);

        LOGGER.info("[TEST_socialLogin] {} {} {}", resource.id(), resource.email(), resource.name());

        PlayerDTO response = playerRepository.findByEmail(resource.email())
                .map(Player::toPlayerDTO)
                .orElseGet(() -> {
                    LOGGER.info("등록되지 않은 google 계정 {}", resource.email());
                    return signService.signUpBySocial(new PlayerDTO(
                            null,
                            resource.email(),
                            resource.name(),
                            "google",
                            resource.id(),
                            0
                    ));
                });


        LOGGER.info("[TEST_socialLogin] {}", response.toString());
        return response;
    }
    private String TEST_getAccessToken(String code){
        LOGGER.info("[TEST_getAccessToken] call {}", code);
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getGOOGLE_CLIENT_ID());
        body.add("client_secret", oAuthKey.getGOOGLE_CLIENT_SECRET());
        body.add("redirect_uri", "http://localhost"+oAuthKey.getGOOGLE_REDIRECT_URI().substring(20));
        System.out.println("http://localhost"+oAuthKey.getGOOGLE_REDIRECT_URI().substring(20));
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LOGGER.info("[TEST_getAccessToken] set body");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        OAuthTokenResponse response = restTemplate.postForObject(oAuthKey.getGOOGLE_TOKEN_URI(), entity, OAuthTokenResponse.class);
        LOGGER.info("[TEST_getAccessToken] get token: {}", response.access_token());

        if (response.access_token() != null){
            return response.access_token();
        }else {
            return null;
        }
    }
    private OAuthResourceResponse TEST_getUserResource(String accessToken) {
        LOGGER.info("[TEST_getUserResource] call");
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        LOGGER.info("[TEST_getUserResource] set entity");

        OAuthResourceResponse response = restTemplate.exchange(oAuthKey.getGOOGLE_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceResponse.class).getBody();

        if (response != null){
            LOGGER.info("[TEST_getUserResource] get user resource {}", response);
            return response;
        }else {
            return null;
        }
    }

}
