package org.codequistify.master.virtualworkspace.domain.model;

import java.util.Objects;
import org.codequistify.master.domain.shared.stage.StageCode;

public record StageSpecSnapshot(
    StageCode stageCode, String image, int port, String cpu, String memory, String readinessPath) {
  public StageSpecSnapshot {
    Objects.requireNonNull(stageCode, "stageCode must not be null");
    image = requireNonBlank(image, "image must not be blank");
    cpu = requireNonBlank(cpu, "cpu must not be blank");
    memory = requireNonBlank(memory, "memory must not be blank");
    readinessPath = requireNonBlank(readinessPath, "readinessPath must not be blank");

    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("port must be between 1 and 65535");
    }
    if (!readinessPath.startsWith("/")) {
      throw new IllegalArgumentException("readinessPath must start with '/'");
    }
  }

  public static StageSpecSnapshot of(
      StageCode stageCode,
      String image,
      int port,
      String cpu,
      String memory,
      String readinessPath) {
    return new StageSpecSnapshot(stageCode, image, port, cpu, memory, readinessPath);
  }

  private static String requireNonBlank(String value, String message) {
    Objects.requireNonNull(value, message);
    String trimmed = value.trim();
    if (trimmed.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return trimmed;
  }
}
