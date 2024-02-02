package org.codequistify.master.domain.stage.domain;

import lombok.Getter;

@Getter
public enum DifficultyLevel {
    VERY_EASY(1),
    EASY(2),
    NORMAL(3),
    HARD(4),
    VERY_HARD(5);

    private final int level;
    DifficultyLevel(int level) {
        this.level = level;
    }
}