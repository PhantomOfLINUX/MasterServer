package org.codequistify.master.infrastructure.account.converter;

import org.codequistify.master.core.domain.account.model.EmailVerification;
import org.codequistify.master.infrastructure.account.entity.EmailVerificationEntity;

public class EmailVerificationConverter {

    public static EmailVerification toDomain(EmailVerificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return EmailVerification.builder()
                                .id(entity.getId())
                                .email(entity.getEmail())
                                .code(entity.getCode())
                                .verified(entity.isVerified())
                                .used(entity.isUsed())
                                .emailVerificationType(entity.getEmailVerificationType())
                                .build();
    }

    public static EmailVerificationEntity toEntity(EmailVerification domain) {
        if (domain == null) {
            return null;
        }

        return EmailVerificationEntity.builder()
                                      .email(domain.getEmail())
                                      .code(domain.getCode())
                                      .emailVerificationType(domain.getEmailVerificationType())
                                      .build();
    }
}
