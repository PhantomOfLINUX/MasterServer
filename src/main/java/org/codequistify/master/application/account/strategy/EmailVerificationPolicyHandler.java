package org.codequistify.master.application.account.strategy;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailVerificationPolicyHandler {

    private final List<EmailVerificationPolicy> policies;

    public void validate(String email, EmailVerificationType type) {
        policies.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.EMAIL_SENDING_FAILURE, HttpStatus.BAD_REQUEST))
                .validate(email);
    }
}