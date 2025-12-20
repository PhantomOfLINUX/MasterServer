package org.codequistify.master.virtualworkspace.domain.vo;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;

public record WorkspacePublicId(String value) {
  private static final int MAX_LENGTH = 63;
  private static final String DNS_LABEL_REGEX = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$";
  private static final int DEFAULT_SIZE = 21;
  private static final char[] SAFE_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
  private static final SecureRandom RANDOM = new SecureRandom();

  public WorkspacePublicId {
    Objects.requireNonNull(value, "publicId must not be null");
    value = normalize(value);
    validate(value);
  }

  public static WorkspacePublicId from(String value) {
    return new WorkspacePublicId(value);
  }

  public static WorkspacePublicId issue() {
    return issue(DEFAULT_SIZE);
  }

  public static WorkspacePublicId issue(int size) {
    if (size <= 0 || size > MAX_LENGTH) {
      throw new IllegalArgumentException("publicId size must be between 1 and 63");
    }

    char[] buffer = new char[size];
    for (int i = 0; i < size; i++) {
      buffer[i] = SAFE_CHARSET[RANDOM.nextInt(SAFE_CHARSET.length)];
    }

    return new WorkspacePublicId(new String(buffer));
  }

  private static String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private static void validate(String value) {
    if (value.isBlank()) {
      throw new IllegalArgumentException("publicId must not be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("publicId length must be <= 63");
    }
    if (!value.matches(DNS_LABEL_REGEX)) {
      throw new IllegalArgumentException("publicId must be a valid DNS label");
    }
  }
}
