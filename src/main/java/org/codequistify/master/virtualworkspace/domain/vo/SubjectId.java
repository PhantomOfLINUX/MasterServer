package org.codequistify.master.virtualworkspace.domain.vo;

import java.util.Objects;

public record SubjectId(String value) {
  public SubjectId {
    Objects.requireNonNull(value, "subjectId must not be null");
    value = value.trim();
    if (value.isBlank()) {
      throw new IllegalArgumentException("subjectId must not be blank");
    }
  }

  public static SubjectId from(String value) {
    return new SubjectId(value);
  }
}
