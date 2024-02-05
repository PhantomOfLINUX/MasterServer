package org.codequistify.master.domain.player.domain;

public enum PlayerRoleType {
    PLAYER("ROLE_PLAYER"),
    ADMIN("ROLE_ADMIN"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN");

    private final String role;

    PlayerRoleType(String role) {
        this.role = role;
    }

    public String getRole() {
        return this.role;
    }
}
