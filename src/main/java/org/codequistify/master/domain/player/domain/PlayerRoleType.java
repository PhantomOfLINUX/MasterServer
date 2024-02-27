package org.codequistify.master.domain.player.domain;

import lombok.Getter;

@Getter
public enum PlayerRoleType {
    GUEST("ROLE_GUEST"),
    PLAYER("ROLE_PLAYER"),
    ADMIN("ROLE_ADMIN"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN");

    private final String role;

    PlayerRoleType(String role) {
        this.role = role;
    }
}
