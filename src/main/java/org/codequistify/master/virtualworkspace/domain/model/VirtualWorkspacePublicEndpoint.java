package org.codequistify.master.virtualworkspace.domain.model;

import java.util.Locale;
import java.util.Objects;
import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;

public record VirtualWorkspacePublicEndpoint(WorkspacePublicId publicId, String baseHost) {
  public VirtualWorkspacePublicEndpoint {
    Objects.requireNonNull(publicId, "publicId must not be null");
    baseHost = normalizeBaseHost(baseHost);
    validateBaseHost(baseHost);
  }

  public static VirtualWorkspacePublicEndpoint of(WorkspacePublicId publicId, String baseHost) {
    return new VirtualWorkspacePublicEndpoint(publicId, baseHost);
  }

  public String value() {
    return publicId.value() + "." + baseHost;
  }

  private static String normalizeBaseHost(String baseHost) {
    Objects.requireNonNull(baseHost, "baseHost must not be null");
    return baseHost.trim().toLowerCase(Locale.ROOT);
  }

  private static void validateBaseHost(String baseHost) {
    if (baseHost.isBlank()) {
      throw new IllegalArgumentException("baseHost must not be blank");
    }
    if (baseHost.contains("://") || baseHost.contains("/") || baseHost.contains("?")) {
      throw new IllegalArgumentException("baseHost must be host only");
    }
    if (baseHost.startsWith(".") || baseHost.endsWith(".")) {
      throw new IllegalArgumentException("baseHost must not start/end with '.'");
    }
  }
}
