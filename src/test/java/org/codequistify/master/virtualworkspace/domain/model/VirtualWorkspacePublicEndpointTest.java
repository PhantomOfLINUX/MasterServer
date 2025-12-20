package org.codequistify.master.virtualworkspace.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.codequistify.master.virtualworkspace.domain.vo.WorkspacePublicId;
import org.junit.jupiter.api.Test;

class VirtualWorkspacePublicEndpointTest {

  @Test
  void baseHost는_트림되고_소문자로_정규화되어야한다() {
    VirtualWorkspacePublicEndpoint endpoint =
        VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), " LAB.POL.OR.KR ");

    assertEquals("lab.pol.or.kr", endpoint.baseHost());
  }

  @Test
  void value는_publicId와_baseHost로_구성되어야한다() {
    VirtualWorkspacePublicEndpoint endpoint =
        VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), "lab.pol.or.kr");

    assertEquals("vw-1.lab.pol.or.kr", endpoint.value());
  }

  @Test
  void baseHost에_스킴이_포함되면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            VirtualWorkspacePublicEndpoint.of(
                WorkspacePublicId.from("vw-1"), "https://lab.pol.or.kr"));
  }

  @Test
  void baseHost가_빈값이면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), " "));
  }

  @Test
  void baseHost가_점으로_시작하면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), ".lab.pol.or.kr"));
  }

  @Test
  void baseHost가_점으로_끝나면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VirtualWorkspacePublicEndpoint.of(WorkspacePublicId.from("vw-1"), "lab.pol.or.kr."));
  }
}
