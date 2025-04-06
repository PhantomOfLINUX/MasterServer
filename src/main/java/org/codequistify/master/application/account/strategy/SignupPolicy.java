package org.codequistify.master.application.account.strategy;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.application.player.service.PlayerProfileService;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.service.PlayerValidator;
import org.codequistify.master.core.domain.vo.Email;
import org.codequistify.master.infrastructure.player.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class SignupPolicy {

    private final PlayerProfileService playerProfileService;
    private final PlayerRepository playerRepository;
    private final PlayerValidator playerValidator;

    public void validate(String name, Email email, String password) {
        validateEmailNotTaken(email);
        validatePassword(password);
        validateName(name);
        validateNameUniqueness(name);
    }

    private void validateEmailNotTaken(Email email) {
        playerRepository.getOAuthTypeByEmail(email).ifPresent(authType -> {
            ErrorCode code = authType.equals(OAuthType.POL)
                    ? ErrorCode.EMAIL_ALREADY_EXISTS
                    : ErrorCode.EMAIL_ALREADY_EXISTS_OTHER_AUTH;
            throw new ApplicationException(code, HttpStatus.BAD_REQUEST, authType.name());
        });
    }

    private void validatePassword(String password) {
        Predicate<String> validPassword = playerValidator::isValidPassword;
        if (!validPassword.test(password)) {
            throw new ApplicationException(ErrorCode.PASSWORD_POLICY_VIOLATION, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateName(String name) {
        Predicate<String> validName = playerValidator::isValidName;
        if (!validName.test(name)) {
            throw new ApplicationException(ErrorCode.PROFANITY_IN_NAME, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateNameUniqueness(String name) {
        if (playerProfileService.isDuplicatedName(name)) {
            throw new ApplicationException(ErrorCode.DUPLICATE_NAME, HttpStatus.BAD_REQUEST);
        }
    }
}
