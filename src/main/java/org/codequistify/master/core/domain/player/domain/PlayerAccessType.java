package org.codequistify.master.core.domain.player.domain;

import lombok.Getter;

@Getter
public enum PlayerAccessType {
    BASIC_PROBLEMS_ACCESS("ROLE_BASIC_PROBLEMS_ACCESS"),
    ADVANCED_PROBLEMS_ACCESS("ROLE_ADVANCED_PROBLEMS_ACCESS"),
    MOCK_TESTS_ACCESS("ROLE_MOCK_TESTS_ACCESS");

    private final String permission;

    PlayerAccessType(String permission) {
        this.permission = permission;
    }
}
