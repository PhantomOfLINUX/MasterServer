package org.codequistify.master.domain.lab.config;

public final class LabInfrastructureDefaults {
    public static final String LAB_NAMESPACE = "default";
    public static final int LAB_SERVICE_PORT = 8080;
    public static final int LAB_READINESS_PORT = 8080;
    public static final String LAB_READINESS_PATH = "/health";

    private LabInfrastructureDefaults() {
    }
}
