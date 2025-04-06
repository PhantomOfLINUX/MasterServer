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

        check(password)
                .with(playerValidator::isValidPassword, ErrorCode.PASSWORD_POLICY_VIOLATION)
                .orThrow();

        check(name)
                .with(playerValidator::isValidName, ErrorCode.PROFANITY_IN_NAME)
                .with(n -> !playerProfileService.isDuplicatedName(n), ErrorCode.DUPLICATE_NAME)
                .orThrow();
    }

    private void validateEmailNotTaken(Email email) {
        playerRepository.getOAuthTypeByEmail(email).ifPresent(authType -> {
            ErrorCode code = authType.equals(OAuthType.POL)
                    ? ErrorCode.EMAIL_ALREADY_EXISTS
                    : ErrorCode.EMAIL_ALREADY_EXISTS_OTHER_AUTH;
            throw new ApplicationException(code, HttpStatus.BAD_REQUEST, authType.name());
        });
    }

    private <T> ValidatorChain<T> check(T target) {
        return new ValidatorChain<>(target);
    }

    private static class ValidatorChain<T> {
        private final T target;
        private ApplicationException failure;

        public ValidatorChain(T target) {
            this.target = target;
        }

        public ValidatorChain<T> with(Predicate<T> predicate, ErrorCode errorCode) {
            return with(predicate, errorCode, HttpStatus.BAD_REQUEST);
        }

        public ValidatorChain<T> with(Predicate<T> predicate, ErrorCode errorCode, HttpStatus status) {
            if (failure == null && !predicate.test(target)) {
                failure = new ApplicationException(errorCode, status);
            }
            return this;
        }

        public void orThrow() {
            if (failure != null) {
                throw failure;
            }
        }
    }
}
