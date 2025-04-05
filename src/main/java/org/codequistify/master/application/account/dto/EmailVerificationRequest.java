package org.codequistify.master.application.account.dto;

import jakarta.validation.constraints.NotNull;
import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.codequistify.master.core.domain.vo.Email;

public record EmailVerificationRequest(
        Email email,
        @NotNull(message = "4101") EmailVerificationType type
) {
}
