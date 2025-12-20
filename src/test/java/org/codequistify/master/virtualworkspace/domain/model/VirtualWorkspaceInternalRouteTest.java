package org.codequistify.master.virtualworkspace.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class VirtualWorkspaceInternalRouteTest {

  @Test
  void namespace와_serviceName은_트림되어야한다() {
    VirtualWorkspaceInternalRoute route =
        VirtualWorkspaceInternalRoute.of(" default ", " svc-name ", 8080);

    assertEquals("default", route.namespace());
    assertEquals("svc-name", route.serviceName());
  }

  @Test
  void servicePort가_유효범위_밖이면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VirtualWorkspaceInternalRoute.of("default", "svc-name", 0));
  }

  @Test
  void namespace가_빈값이면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VirtualWorkspaceInternalRoute.of(" ", "svc-name", 8080));
  }
}
