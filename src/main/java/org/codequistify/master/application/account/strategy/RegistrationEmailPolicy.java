package org.codequistify.master.application.account.strategy;

import org.codequistify.master.application.account.service.AccountService;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.codequistify.master.core.domain.vo.Email;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RegistrationEmailPolicy implements EmailVerificationPolicy {

    private final AccountService accountService;

    public RegistrationEmailPolicy(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean supports(EmailVerificationType type) {
        return type == EmailVerificationType.REGISTRATION;
    }

    @Override
    public void validate(Email email) {
        if (accountService.checkEmailDuplication(email)) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
    }
}