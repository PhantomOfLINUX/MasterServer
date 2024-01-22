package org.codequistify.master.domain.player.dto;

import org.codequistify.master.domain.player.domain.Player;

public record PlayerDTO(
        Long id,
        String email,
        String name,
        String authType,
        Long authId,
        Integer level
) {
    public PlayerDTO of(Long id, String email, String name, String authType, Long authId, Integer level) {
        return new PlayerDTO(id, email, name, authType, authId, level);
    }

    public Player toPlayer(String password) {
        return Player.builder()
                .email(this.email)
                .name(this.name)
                .authType(this.authType)
                .authId(this.authId)
                .level(this.level)
                .password(password)
                .build();
    }
}
