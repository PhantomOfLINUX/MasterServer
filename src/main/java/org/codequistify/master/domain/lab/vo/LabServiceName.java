package org.codequistify.master.domain.lab.vo;

import java.util.Locale;
import java.util.Objects;

public record LabServiceName(String value) {
    private static final int MAX_LENGTH = 63;
    private static final String DNS_1035_REGEX = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$";
    private static final String SERVICE_DNS_FORMAT = "%s.%s.svc.cluster.local";
    private static final String NAME_FORMAT = "%s-%s";

    public LabServiceName {
        Objects.requireNonNull(value, "serviceName must not be null");
        value = normalize(value);
        validate(value);
    }

    public static LabServiceName from(String value) {
        return new LabServiceName(value);
    }

    public static LabServiceName issue(StageCode stageCode, LabUserUid uid) {
        Objects.requireNonNull(stageCode, "stageCode must not be null");
        Objects.requireNonNull(uid, "uid must not be null");

        String issuedName = NAME_FORMAT.formatted(stageCode.lowercase(), uid.value());
        issuedName = trimToLength(issuedName);

        return new LabServiceName(issuedName);
    }

    public String serviceDns(String namespace) {
        Objects.requireNonNull(namespace, "namespace must not be null");
        return SERVICE_DNS_FORMAT.formatted(this.value, namespace);
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }

    private static void validate(String value) {
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Kubernetes service name 길이는 63자를 초과할 수 없습니다");
        }

        if (!value.matches(DNS_1035_REGEX)) {
            throw new IllegalArgumentException("Kubernetes service name 형식이 올바르지 않습니다");
        }
    }

    private static String trimToLength(String value) {
        if (value.length() <= MAX_LENGTH) {
            return value;
        }

        String trimmed = value.substring(0, MAX_LENGTH);
        if (trimmed.endsWith("-")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
