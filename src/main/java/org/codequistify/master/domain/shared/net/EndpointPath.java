package org.codequistify.master.domain.shared.net;

import java.util.Objects;

public record EndpointPath(String value) {
  public EndpointPath {
    Objects.requireNonNull(value, "path must not be null");
    value = normalize(value);
    validate(value);
  }

  public static EndpointPath of(String value) {
    return new EndpointPath(value);
  }

  private static String normalize(String value) {
    return value.trim();
  }

  private static void validate(String value) {
    if (value.isBlank()) {
      throw new IllegalArgumentException("path must not be blank");
    }
    if (!value.startsWith("/")) {
      throw new IllegalArgumentException("path must start with '/'");
    }
    if (value.contains("://") || value.contains("?")) {
      throw new IllegalArgumentException("path must be path only");
    }
  }
}
