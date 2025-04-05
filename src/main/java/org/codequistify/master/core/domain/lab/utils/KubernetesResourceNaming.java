package org.codequistify.master.core.domain.lab.utils;

public class KubernetesResourceNaming {
    //private static final String POD_NAME_FORMAT = "%s-pod-%s";
    private static final String POD_NAME_FORMAT = "%s-%s";
    private static final String QUERY_URL_FORMAT = "?uid=%s&stage=%s";
    private static final String SERVICE_DNS_FORMAT = "%s.%s.svc.cluster.local";
    //private static final String SERVICE_NAME_FORMAT = "%s-svc-%s";
    private static final String SERVICE_NAME_FORMAT = "%s-%s";

    public static String getPodName(String stageCode, String uid) {
        return String.format(POD_NAME_FORMAT, stageCode.toLowerCase(), uid.toLowerCase());
    }

    public static String getServiceName(String stageCode, String uid) {
        return String.format(SERVICE_NAME_FORMAT, stageCode.toLowerCase(), uid.toLowerCase());
    }

    public static String getServiceDNS(String svcName, String namespace) {
        return String.format(SERVICE_DNS_FORMAT, svcName, namespace);
    }

    public static String getQuery(String stageCode, String uid) {
        return String.format(QUERY_URL_FORMAT, uid, stageCode.toLowerCase());
    }
}
