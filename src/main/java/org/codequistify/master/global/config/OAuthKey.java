package org.codequistify.master.global.config;

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
}
