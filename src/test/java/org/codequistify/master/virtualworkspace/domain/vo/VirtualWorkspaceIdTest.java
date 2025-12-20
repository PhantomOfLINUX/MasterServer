package org.codequistify.master.virtualworkspace.domain.vo;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.codequistify.master.domain.player.domain.PlayerId;
import org.codequistify.master.domain.shared.stage.StageCode;
import org.junit.jupiter.api.Test;

class VirtualWorkspaceIdTest {

  @Test
  void playerId가_null이면_생성이_실패해야한다() {
    assertThrows(NullPointerException.class, () -> VirtualWorkspaceId.of(null, StageCode.from("S1001")));
  }

  @Test
  void stageCode가_null이면_생성이_실패해야한다() {
    assertThrows(NullPointerException.class, () -> VirtualWorkspaceId.of(PlayerId.of(1L), null));
  }
}
