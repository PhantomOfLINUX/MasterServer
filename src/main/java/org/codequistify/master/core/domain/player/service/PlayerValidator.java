package org.codequistify.master.core.domain.player.service;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PlayerValidator {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&])[a-z\\d@$!%*#?&]{8,}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z가-힣0-9]+$");

    private final ProfanityChecker profanityChecker;

    public boolean isValidName(String name) {
        return NAME_PATTERN.matcher(name).matches() && !profanityChecker.hasProfanity(name);
    }

    public boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
