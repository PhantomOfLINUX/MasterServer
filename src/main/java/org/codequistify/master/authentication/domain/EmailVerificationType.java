package org.codequistify.master.domain.authentication.domain;

import lombok.Getter;

@Getter
public enum EmailVerificationType {
    REGISTRATION("회원가입"),
    PASSWORD_RESET("비밀번호 재설정");

    private final String description;

    EmailVerificationType(String description) {
        this.description = description;
    }

}
