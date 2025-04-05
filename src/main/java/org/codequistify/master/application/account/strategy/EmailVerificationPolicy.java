package org.codequistify.master.application.account.strategy;

import org.codequistify.master.core.domain.account.model.EmailVerificationType;

public interface EmailVerificationPolicy {
    boolean supports(EmailVerificationType type);
    void validate(String email);
}