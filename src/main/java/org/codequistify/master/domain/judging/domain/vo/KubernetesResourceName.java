package org.codequistify.master.domain.judging.domain.vo;

import org.codequistify.master.global.data.Pair;
import org.codequistify.master.global.data.UrlQuery;

public record KubernetesResourceName(
        StageCode stageCode,
        LabUserUid uid,
        LabServiceName serviceName
) {
    private static final String POD_NAME_FORMAT = "%s-%s";
    private static final String SERVICE_DNS_FORMAT = "%s.%s.svc.cluster.local";

    public static KubernetesResourceName of(StageCode stageCode, LabUserUid uid) {
        return of(stageCode, uid, LabServiceName.issue(stageCode, uid));
    }

    public static KubernetesResourceName of(StageCode stageCode, LabUserUid uid, LabServiceName serviceName) {
        return new KubernetesResourceName(stageCode, uid, serviceName);
    }

    public String podName() {
        return POD_NAME_FORMAT.formatted(stageCode.lowercase(), uid.value());
    }

    public String serviceDns(String namespace) {
        return SERVICE_DNS_FORMAT.formatted(serviceName.value(), namespace);
    }

    public UrlQuery query() {
        return UrlQuery.from(
                Pair.of("uid", uid.value()),
                Pair.of("stage", stageCode.lowercase())
        );
    }
}
