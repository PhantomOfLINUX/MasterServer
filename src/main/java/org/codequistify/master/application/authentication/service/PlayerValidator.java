package org.codequistify.master.application.authentication.service;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.player.model.Player;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class PlayerValidator {
    private final BadWordFiltering badWordFiltering;
    private final static String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&])[a-z\\d@$!%*#?&]{8,}$";
    private final static String NAME_REGEX = "^[a-zA-Z가-힣0-9]+$";
    private final static Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private final static Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    public boolean isValidName(Player player) {
        Matcher matcher = NAME_PATTERN.matcher(player.getName());
        if (matcher.matches()) {
            return !badWordFiltering.check(player.getName());
        }
        return false;
    }
    public boolean isValidName(String name) {
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (matcher.matches()) {
            return !badWordFiltering.check(name);
        }
        return false;
    }

    public boolean isValidPassword(Player player) {
        Matcher matcher = PASSWORD_PATTERN.matcher(player.getPassword());
        return matcher.matches();
    }
    public boolean isValidPassword(String password) {
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }

}
