package org.codequistify.master.infrastructure.account.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class OAuthKey {
    // Google
    @Value("${oauth2.google.client-id}")
    private String GOOGLE_CLIENT_ID;
    @Value("${oauth2.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;
    @Value("${oauth2.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URI;
    @Value("${oauth2.google.token-uri}")
    private String GOOGLE_TOKEN_URI;
    @Value("${oauth2.google.resource-uri}")
    private String GOOGLE_RESOURCE_URI;

    // Kakao
    @Value("${oauth2.kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    @Value("${oauth2.kakao.redirect-uri}")
    private String KAKAO_REDIRECT_URI;
    @Value("${oauth2.kakao.token-uri}")
    private String KAKAO_TOKEN_URI;
    @Value("${oauth2.kakao.resource-uri}")
    private String KAKAO_RESOURCE_URI;

    // Naver
    @Value("${oauth2.naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${oauth2.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;
    @Value("${oauth2.naver.redirect-uri}")
    private String NAVER_REDIRECT_URI;
    @Value("${oauth2.naver.token-uri}")
    private String NAVER_TOKEN_URI;
    @Value("${oauth2.naver.resource-uri}")
    private String NAVER_RESOURCE_URI;

    // Github
    @Value("${oauth2.github.client-id}")
    private String GITHUB_CLIENT_ID;
    @Value("${oauth2.github.client-secret}")
    private String GITHUB_CLIENT_SECRET;
    @Value("${oauth2.github.redirect-uri}")
    private String GITHUB_REDIRECT_URI;
    @Value("${oauth2.github.token-uri}")
    private String GITHUB_TOKEN_URI;
    @Value("${oauth2.github.resource-uri}")
    private String GITHUB_RESOURCE_URI;
}
