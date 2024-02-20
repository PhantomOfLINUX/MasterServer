package org.codequistify.master.domain.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.service.SocialSignService;
import org.codequistify.master.domain.authentication.vo.OAuthResourceVO;
import org.codequistify.master.domain.authentication.vo.OAuthTokenVO;
import org.codequistify.master.domain.player.converter.PlayerConverter;
import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.service.PlayerDetailsService;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NaverSocialSignService implements SocialSignService {
    private final PlayerDetailsService playerDetailsService;
    private final RestTemplate restTemplate;

    private final PlayerConverter playerConverter;
    private final Logger LOGGER = LoggerFactory.getLogger(NaverSocialSignService.class);
    private final OAuthKey oAuthKey;

    /*
    네이버 소셜 로그인 주소 반환
     */
    @Override
    public String getSocialLogInURL() {
        return "https://nid.naver.com/oauth2.0/authorize?" +
                "client_id=" + oAuthKey.getNAVER_CLIENT_ID() +
                "&redirect_uri=" + URLEncoder.encode(oAuthKey.getNAVER_REDIRECT_URI(), StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state=" + URLEncoder.encode("pol", StandardCharsets.UTF_8);
    }

    /*
    code를 통한 소셜 로그인
     */
    @Override
    @Transactional
    public PlayerProfile socialLogIn(String code) {
        String accessToken = getAccessToken(code);
        OAuthResourceVO resource = getUserResource(accessToken);
        Map<String, String> naverResponse = resource.response();

        LOGGER.info("{} {}", resource.id(), naverResponse.get("name"));

        Player player;
        try {
            player = playerDetailsService.findOndPlayerByEmail(naverResponse.get("email"));
        } catch (BusinessException exception) {
            LOGGER.info("등록되지 않은 네이버 계정 {}", naverResponse.get("email"));
            player = this.socialSignUp(resource);
        }

        player.updateOAuthAccessToken(accessToken);

        playerDetailsService.save(player);

        PlayerProfile response = playerConverter.convert(player);
        LOGGER.info("[socialLogin] {} 카카오 로그인", player.getEmail());

        return response;
    }

    @Override
    public Player socialSignUp(OAuthResourceVO resource) {
        Map<String, String> response = Objects.requireNonNull(resource).response();
        Player player = Player.builder()
                .name(response.get("name"))
                .email(response.get("email"))
                .oAuthType(OAuthType.NAVER)
                .oAuthId(resource.id()).build();
        player = playerDetailsService.save(player);
        LOGGER.info("[socialSignUp] 신규 네이버 사용자 등록");
        return player;
    }

    private String getAccessToken(String code) {
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
            OAuthTokenVO response = restTemplate.postForObject(oAuthKey.getNAVER_TOKEN_URI(), entity, OAuthTokenVO.class);
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
            Map<String, String> map = restTemplate.exchange(oAuthKey.getNAVER_RESOURCE_URI(), HttpMethod.GET, entity, OAuthResourceVO.class).getBody().response();
            OAuthResourceVO response = new OAuthResourceVO(map.get("id"), map.get("email"), convertUnicodeToString(map.get("name")), null, null);
            return response;
        } catch (NullPointerException | RestClientException exception) {
            LOGGER.info("[getUserResource] 정보 요청 실패");
            throw new BusinessException(ErrorCode.OAUTH_COMMUNICATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static String convertUnicodeToString(String unicode) {
        StringBuilder string = new StringBuilder();
        String[] codeString = unicode.split("\\\\u");
        for (int i = 1; i < codeString.length; i++) {
            int code = Integer.parseInt(codeString[i], 16);
            string.append((char) code);
        }
        return string.toString();
    }
}
