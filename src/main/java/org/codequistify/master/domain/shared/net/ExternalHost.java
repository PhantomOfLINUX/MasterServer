package org.codequistify.master.domain.shared.net;

import java.util.Locale;
import java.util.Objects;

public record ExternalHost(String value) {
  public ExternalHost {
    Objects.requireNonNull(value, "host must not be null");
    value = normalize(value);
    validate(value);
  }

  public static ExternalHost of(String value) {
    return new ExternalHost(value);
  }

  private static String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private static void validate(String value) {
    if (value.isBlank()) {
      throw new IllegalArgumentException("host must not be blank");
    }
    if (value.contains("://") || value.contains("/") || value.contains("?")) {
      throw new IllegalArgumentException("host must be host only");
    }
    if (value.startsWith(".") || value.endsWith(".")) {
      throw new IllegalArgumentException("host must not start/end with '.'");
    }
  }
}
