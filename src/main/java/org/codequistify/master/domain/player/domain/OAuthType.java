package org.codequistify.master.domain.player.domain;

import lombok.Getter;

@Getter
public enum OAuthType {
    POL(0, "자체"),
    GOOGLE(1, "구글"),
    KAKAO(2, "카카오"),
    NAVER(3, "네이버"),
    GITHUB(4, "깃허브");

    private final int code;
    private final String title;

    OAuthType(int code, String title) {
        this.code = code;
        this.title = title;
    }
}
