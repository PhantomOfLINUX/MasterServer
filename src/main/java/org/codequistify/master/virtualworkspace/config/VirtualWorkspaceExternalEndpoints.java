package org.codequistify.master.virtualworkspace.config;

import org.codequistify.master.domain.shared.net.ExternalHost;
import org.codequistify.master.domain.shared.net.UrlBuilder;
import org.codequistify.master.domain.shared.net.UrlScheme;
import org.codequistify.master.virtualworkspace.domain.model.VirtualWorkspacePublicEndpoint;

public final class VirtualWorkspaceExternalEndpoints {
  private VirtualWorkspaceExternalEndpoints() {}

  public static String websocketUrl(VirtualWorkspacePublicEndpoint endpoint) {
    ExternalHost host = ExternalHost.of(endpoint.value());
    return UrlBuilder.build(UrlScheme.WSS, host);
  }
}
