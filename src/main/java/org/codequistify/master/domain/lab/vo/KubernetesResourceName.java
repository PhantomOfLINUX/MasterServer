package org.codequistify.master.domain.lab.vo;

import org.codequistify.master.global.data.Pair;
import org.codequistify.master.global.data.UrlQuery;

public record KubernetesResourceName(
        StageCode stageCode,
        LabUserUid uid
) {
    private static final String POD_NAME_FORMAT = "%s-%s";
    private static final String SERVICE_NAME_FORMAT = "%s-%s";
    private static final String SERVICE_DNS_FORMAT = "%s.%s.svc.cluster.local";

    public static KubernetesResourceName of(StageCode stageCode, LabUserUid uid) {
        return new KubernetesResourceName(stageCode, uid);
    }

    public String podName() {
        return POD_NAME_FORMAT.formatted(stageCode.lowercase(), uid.value());
    }

    public String serviceName() {
        return SERVICE_NAME_FORMAT.formatted(stageCode.lowercase(), uid.value());
    }

    public String serviceDns(String namespace) {
        return SERVICE_DNS_FORMAT.formatted(serviceName(), namespace);
    }

    public UrlQuery query() {
        return UrlQuery.from(
                Pair.of("uid", uid.value()),
                Pair.of("stage", stageCode.lowercase())
        );
    }
}

