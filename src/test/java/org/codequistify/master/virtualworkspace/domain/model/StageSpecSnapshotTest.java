package org.codequistify.master.virtualworkspace.domain.model;

import org.codequistify.master.domain.shared.stage.StageCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StageSpecSnapshotTest {

    @Test
    void port가_유효하지_않으면_생성이_실패해야한다() {
        assertThrows(IllegalArgumentException.class, () -> StageSpecSnapshot.of(
                StageCode.from("S1015"),
                "image",
                0,
                "100m",
                "128Mi",
                "/health"
        ));
    }

    @Test
    void readinessPath가_슬래시로_시작하지_않으면_생성이_실패해야한다() {
        assertThrows(IllegalArgumentException.class, () -> StageSpecSnapshot.of(
                StageCode.from("S1015"),
                "image",
                8080,
                "100m",
                "128Mi",
                "health"
        ));
    }
}
