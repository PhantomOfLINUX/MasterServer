package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum DifficultyLevelType {
    L1(1),
    L2(2),
    L3(3),
    L4(4),
    L5(5);

    private final int level;
    DifficultyLevelType(int level) {
        this.level = level;
    }
}
