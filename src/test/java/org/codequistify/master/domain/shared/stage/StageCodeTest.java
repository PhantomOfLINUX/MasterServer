package org.codequistify.master.domain.shared.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StageCodeTest {

  @Test
  void stageCode가_빈값인_경우에는_생성이_실패해야한다() {
    assertThrows(IllegalArgumentException.class, () -> StageCode.from(" "));
  }

  @Test
  void stageCode는_lowercase_표현을_가져야한다() {
    StageCode stageCode = StageCode.from("S1001");
    assertEquals("S1001", stageCode.lowercase());
  }
}
