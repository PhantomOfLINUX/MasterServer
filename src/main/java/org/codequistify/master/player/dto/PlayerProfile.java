package org.codequistify.master.domain.player.dto;

import java.util.List;

public record PlayerProfile(
        String uid,
        String name,
        String email,
        Integer level,
        List<String> roles
) {
}
