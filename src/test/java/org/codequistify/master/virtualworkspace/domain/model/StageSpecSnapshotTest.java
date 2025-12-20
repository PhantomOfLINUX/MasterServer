package org.codequistify.master.virtualworkspace.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.codequistify.master.domain.shared.stage.StageCode;
import org.junit.jupiter.api.Test;

class StageSpecSnapshotTest {

  @Test
  void 스펙_정보는_트림된_값으로_저장되어야한다() {
    StageSpecSnapshot snapshot = StageSpecSnapshot.of(
        StageCode.from("S1001"), " image ", 8080, " 100m ", " 128Mi ", " /health ");

    assertEquals("image", snapshot.image());
    assertEquals("100m", snapshot.cpu());
    assertEquals("128Mi", snapshot.memory());
    assertEquals("/health", snapshot.readinessPath());
  }

  @Test
  void port가_유효하지_않으면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            StageSpecSnapshot.of(StageCode.from("S1001"), "image", 0, "100m", "128Mi", "/health"));
  }

  @Test
  void image가_빈값이면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            StageSpecSnapshot.of(StageCode.from("S1001"), " ", 8080, "100m", "128Mi", "/health"));
  }

  @Test
  void readinessPath가_슬래시로_시작하지_않으면_생성이_실패해야한다() {
    assertThrows(
        IllegalArgumentException.class,
        () -> StageSpecSnapshot.of(
            StageCode.from("S1001"), "image", 8080, "100m", "128Mi", "health"));
  }
}
