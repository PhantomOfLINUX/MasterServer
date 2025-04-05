package org.codequistify.master.core.domain.account.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.core.domain.vo.Email;

@Getter
@ToString
@Builder(toBuilder = true)
public class EmailVerification {
    private final Long id;

    private final Email email;
    private final String code;
    private final boolean verified;
    private final boolean used;
    private final EmailVerificationType emailVerificationType;

    public EmailVerification markAsVerified() {
        return this.toBuilder()
                   .verified(true)
                   .build();
    }

    public EmailVerification markAsUsed() {
        return this.toBuilder()
                   .used(true)
                   .build();
    }

    public static EmailVerification of(Email email, String code, EmailVerificationType type) {
        return EmailVerification.builder()
                                .email(email)
                                .code(code)
                                .verified(false)
                                .used(false)
                                .emailVerificationType(type)
                                .build();
    }
}