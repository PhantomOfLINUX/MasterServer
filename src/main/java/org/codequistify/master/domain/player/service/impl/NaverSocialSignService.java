package org.codequistify.master.domain.player.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.domain.repository.PlayerRepository;
import org.codequistify.master.domain.player.dto.sign.OAuthResourceResponse;
import org.codequistify.master.domain.player.dto.sign.OAuthTokenResponse;
import org.codequistify.master.domain.player.dto.sign.PlayerDTO;
import org.codequistify.master.domain.player.dto.sign.SignInResponse;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NaverSocialSignService implements SocialSignService {
    private final SignService signService;
    private final PlayerRepository playerRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(NaverSocialSignService.class);
    private final OAuthKey oAuthKey;

    /*
    네이버 소셜 로그인 주소 반환
     */
    @Override
    public String getSocialSignInURL(){
        return "https://nid.naver.com/oauth2.0/authorize?" +
                "client_id="+oAuthKey.getNAVER_CLIENT_ID() +
                "&redirect_uri="+ URLEncoder.encode(oAuthKey.getNAVER_REDIRECT_URI(), StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state="+URLEncoder.encode("pol", StandardCharsets.UTF_8);
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
            LOGGER.info("[socialLogin] 등록되지 않은 네이버 계정 {}", resource.email());
            signService.signUpBySocial(new PlayerDTO(
                    null,
                    resource.email(),
                    resource.name(),
                    "naver",
                    resource.id(),
                    0)
            );

            playerOptional = playerRepository.findByEmail(resource.email());
            LOGGER.info("[socialLogin] 등록");
        }

        Player player = playerOptional.get();
        player.updateOAuthAccessToken(accessToken);

        playerRepository.save(player);

        SignInResponse response = player.toSignInResponse();
        LOGGER.info("[socialLogin] {} 네이버 로그인", player.getEmail());

        return response;
    }

    private String getAccessToken(String code){
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getNAVER_CLIENT_ID());
        body.add("client_secret", oAuthKey.getNAVER_CLIENT_SECRET());
        body.add("grant_type", "authorization_code");
        body.add("state", "pol");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        OAuthTokenResponse response = restTemplate.postForObject(oAuthKey.getNAVER_TOKEN_URI(), entity, OAuthTokenResponse.class);

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

        Map<String, String> map = restTemplate.exchange(oAuthKey.getNAVER_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceResponse.class).getBody().response();

        OAuthResourceResponse response = new OAuthResourceResponse(map.get("id"), map.get("email"), convertUnicodeToString(map.get("name")), null, null);

        return response;
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
                    LOGGER.info("등록되지 않은 네이버 계정 {}", resource.email());
                    return signService.signUpBySocial(new PlayerDTO(
                            null,
                            resource.email(),
                            resource.name(),
                            "naver",
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
        body.add("client_id", oAuthKey.getNAVER_CLIENT_ID());
        body.add("client_secret", oAuthKey.getNAVER_CLIENT_SECRET());
        body.add("grant_type", "authorization_code");
        body.add("state", "pol");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        LOGGER.info("[TEST_getAccessToken] set body");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        OAuthTokenResponse response = restTemplate.postForObject(oAuthKey.getNAVER_TOKEN_URI(), entity, OAuthTokenResponse.class);
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

        Map<String, String> map = restTemplate.exchange(oAuthKey.getNAVER_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceResponse.class).getBody().response();

        OAuthResourceResponse response = new OAuthResourceResponse(map.get("id"), map.get("email"), convertUnicodeToString(map.get("name")), null, null);

        if (response != null){
            LOGGER.info("[TEST_getUserResource] get user resource {}", response);
            return response;
        }else {
            return null;
        }
    }

    private static String convertUnicodeToString(String unicode) {
        StringBuilder string = new StringBuilder();
        String[] codeStrs = unicode.split("\\\\u");
        for (int i = 1; i < codeStrs.length; i++) {
            int code = Integer.parseInt(codeStrs[i], 16);
            string.append((char) code);
        }
        return string.toString();
    }
}
