package org.codequistify.master.domain.lab.utils;

public class KubernetesResourceNaming {
    private static final String POD_NAME_FORMAT = "%s-pod-%s";
    private static final String SERVICE_NAME_FORMAT = "%s-svc-%s";

    public static String getPodName(String stageCode, String uid) {
        return String.format(POD_NAME_FORMAT, stageCode.toLowerCase(), uid.toLowerCase());
    }

    public static String getServiceName(String stageCode, String uid) {
        return String.format(SERVICE_NAME_FORMAT, stageCode.toLowerCase(), uid.toLowerCase());
    }
}
