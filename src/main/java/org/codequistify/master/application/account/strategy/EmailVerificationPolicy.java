package org.codequistify.master.application.account.strategy;

import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.codequistify.master.core.domain.vo.Email;

public interface EmailVerificationPolicy {
    boolean supports(EmailVerificationType type);

    void validate(Email email);
}