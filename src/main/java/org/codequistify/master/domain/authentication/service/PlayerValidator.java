package org.codequistify.master.domain.authentication.service;

import com.vane.badwordfiltering.BadWordFiltering;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.domain.Player;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PlayerValidator {
  private final BadWordFiltering badWordFiltering;
  private static final String PASSWORD_REGEX =
      "^(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*#?&])[a-z\\d@$!%*#?&]{8,}$";
  private static final String NAME_REGEX = "^[a-zA-Z가-힣0-9]+$";
  private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
  private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

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
