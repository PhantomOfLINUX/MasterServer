package org.codequistify.master.judging.infrastructure.http;

import org.codequistify.master.domain.shared.net.EndpointPath;
import org.codequistify.master.domain.shared.net.ExternalHost;
import org.codequistify.master.domain.shared.net.UrlBuilder;
import org.codequistify.master.domain.shared.net.UrlScheme;
import org.codequistify.master.global.data.UrlQuery;

public final class LabExternalEndpoints {
    private static final ExternalHost LAB_HOST = ExternalHost.of("lab.pol.or.kr");
    private static final EndpointPath GRADE_PATH = EndpointPath.of("/grade");
    private static final EndpointPath COMPOSE_PATH = EndpointPath.of("/compose");

    private LabExternalEndpoints() {
    }

    public static String websocketHost() {
        return UrlBuilder.build(UrlScheme.WSS, LAB_HOST);
    }

    public static String httpsHost() {
        return UrlBuilder.build(UrlScheme.HTTPS, LAB_HOST);
    }

    public static String gradeUrl(UrlQuery query) {
        return UrlBuilder.build(UrlScheme.HTTPS, LAB_HOST, GRADE_PATH, query);
    }

    public static String composeUrl(UrlQuery query) {
        return UrlBuilder.build(UrlScheme.HTTPS, LAB_HOST, COMPOSE_PATH, query);
    }
}
