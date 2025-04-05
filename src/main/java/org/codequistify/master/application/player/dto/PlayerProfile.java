package org.codequistify.master.application.player.dto;

import org.codequistify.master.core.domain.player.model.Player;

import java.util.List;

public record PlayerProfile(
        String uid,
        String name,
        String email,
        Integer level,
        List<String> roles
) {
    public static PlayerProfile from(Player player) {
        return new PlayerProfile(
                player.getUid().getValue(),
                player.getName(),
                player.getEmail(),
                player.getExp(),
                player.getRoles().stream().toList()
        );
    }
}
