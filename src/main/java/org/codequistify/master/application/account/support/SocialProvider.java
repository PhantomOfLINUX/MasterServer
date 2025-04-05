package org.codequistify.master.application.account.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver"),
    GITHUB("github");

    private final String key;

    public static SocialProvider from(String key) {
        for (SocialProvider provider : values()) {
            if (provider.getKey().equalsIgnoreCase(key)) {
                return provider;
            }
        }
        throw new ApplicationException(ErrorCode.OAUTH_COMMUNICATION_FAILURE,
                                       HttpStatus.BAD_REQUEST,
                                       "지원하지 않는 소셜입니다. " + key);
    }
}