package org.codequistify.master.virtualworkspace.config;

public final class VirtualWorkspaceDefaults {
  public static final String BASE_HOST = "lab.pol.or.kr";
  public static final String NAMESPACE = "default";

  public static final int SERVICE_PORT = 8080;
  public static final String READINESS_PATH = "/health";

  public static final String DEFAULT_CPU = "100m";
  public static final String DEFAULT_MEMORY = "128Mi";

  private VirtualWorkspaceDefaults() {}
}
