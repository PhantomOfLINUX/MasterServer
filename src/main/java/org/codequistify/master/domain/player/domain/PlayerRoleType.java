package org.codequistify.master.domain.player.domain;

import lombok.Getter;

@Getter
public enum PlayerRoleType {
    PLAYER("ROLE_PLAYER"),
    ADMIN("ROLE_ADMIN"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    TEMPORARY("TEMPORARY"),
    GUEST("ROLE_GUEST");

    private final String role;

    PlayerRoleType(String role) {
        this.role = role;
    }
}
