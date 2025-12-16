package org.codequistify.master.domain.lab.config;

import org.codequistify.master.global.data.UrlQuery;

public final class LabExternalEndpoints {
    private static final String LAB_DOMAIN = "lab.pol.or.kr";
    private static final String HTTPS_SCHEME = "https://";
    private static final String WSS_SCHEME = "wss://";
    private static final String GRADE_PATH = "/grade";
    private static final String COMPOSE_PATH = "/compose";

    private LabExternalEndpoints() {
    }

    public static String websocketHost() {
        return WSS_SCHEME + LAB_DOMAIN;
    }

    public static String httpsHost() {
        return HTTPS_SCHEME + LAB_DOMAIN;
    }

    public static String gradeUrl(UrlQuery query) {
        return httpsHost() + GRADE_PATH + query.value();
    }

    public static String composeUrl(UrlQuery query) {
        return httpsHost() + COMPOSE_PATH + query.value();
    }
}
