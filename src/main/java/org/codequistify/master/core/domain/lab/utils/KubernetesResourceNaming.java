package org.codequistify.master.core.domain.lab.utils;

import org.codequistify.master.core.domain.player.model.PolId;

public class KubernetesResourceNaming {
    private static final String POD_NAME_FORMAT = "%s-%s";
    private static final String QUERY_URL_FORMAT = "?uid=%s&stage=%s";
    private static final String SERVICE_DNS_FORMAT = "%s.%s.svc.cluster.local";
    private static final String SERVICE_NAME_FORMAT = "%s-%s";

    public static String getPodName(String stageCode, PolId uid) {
        return POD_NAME_FORMAT.formatted(stageCode.toLowerCase(), uid.getValue().toLowerCase());
    }

    public static String getServiceName(String stageCode, PolId uid) {
        return SERVICE_NAME_FORMAT.formatted(stageCode.toLowerCase(), uid.getValue().toLowerCase());
    }

    public static String getServiceDNS(String svcName, String namespace) {
        return SERVICE_DNS_FORMAT.formatted(svcName, namespace);
    }

    public static String getQuery(String stageCode, PolId uid) {
        return QUERY_URL_FORMAT.formatted(uid.getValue().toLowerCase(), stageCode.toLowerCase());
    }
}
