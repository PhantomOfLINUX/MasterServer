package org.codequistify.master.domain.player.service.imple;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.dto.OAuthResourceResponse;
import org.codequistify.master.domain.player.dto.OAuthTokenResponse;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.service.SocialSignService;
import org.codequistify.master.global.config.OAuthKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KakaoSocialSignService implements SocialSignService {
    private final Logger LOGGER = LoggerFactory.getLogger(KakaoSocialSignService.class);
    private final OAuthKey oAuthKey;
    /*
    카카오 소셜 로그인 주소 반환
     */
    public String getSocialSignInURL(){
        return "https://kauth.kakao.com/oauth/authorize?response_type=code" +
                "&client_id="+oAuthKey.getKAKAO_CLIENT_ID() +
                "&redirect_uri="+oAuthKey.getKAKAO_REDIRECT_URI();
    }


    /*
    code를 통한 소셜 로그인
     */
    public PlayerDTO socialLogin(String code) {
        String accessToken = getAccessToken(code);
        OAuthResourceResponse response = getUserResource(accessToken);

        LOGGER.info("{} {}", response.id(), response.properties().get("nickname"));

        return new PlayerDTO(
                response.id(),
                response.email(),
                response.properties().get("nickname")
        );
    }

    private String getAccessToken(String code){
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", oAuthKey.getKAKAO_CLIENT_ID());
        body.add("redirect_uri", oAuthKey.getKAKAO_REDIRECT_URI());
        body.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        OAuthTokenResponse response = restTemplate.postForObject(oAuthKey.getKAKAO_TOKEN_URI(), entity, OAuthTokenResponse.class);

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

        OAuthResourceResponse response = restTemplate.exchange(oAuthKey.getKAKAO_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceResponse.class).getBody();

        if (response != null){
            return response;
        }else {
            return null;
        }
    }

}
