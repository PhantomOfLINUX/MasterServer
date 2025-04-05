package org.codequistify.master.application.account.strategy;

import org.codequistify.master.application.account.service.AccountService;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.codequistify.master.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetEmailPolicy implements EmailVerificationPolicy {

    private final AccountService accountService;

    public PasswordResetEmailPolicy(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean supports(EmailVerificationType type) {
        return type == EmailVerificationType.PASSWORD_RESET;
    }

    @Override
    public void validate(String email) {
        if (!accountService.checkEmailDuplication(email)) {
            throw new ApplicationException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }
}