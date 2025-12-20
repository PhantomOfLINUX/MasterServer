package org.codequistify.master.domain.shared.net;

import java.util.Objects;
import org.codequistify.master.global.data.UrlQuery;

public final class UrlBuilder {
  private UrlBuilder() {}

  public static String build(UrlScheme scheme, ExternalHost host) {
    Objects.requireNonNull(scheme, "scheme must not be null");
    Objects.requireNonNull(host, "host must not be null");
    return scheme.value() + host.value();
  }

  public static String build(
      UrlScheme scheme, ExternalHost host, EndpointPath path, UrlQuery query) {
    Objects.requireNonNull(scheme, "scheme must not be null");
    Objects.requireNonNull(host, "host must not be null");
    Objects.requireNonNull(path, "path must not be null");
    Objects.requireNonNull(query, "query must not be null");
    return scheme.value() + host.value() + path.value() + query.value();
  }
}
