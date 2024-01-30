package org.codequistify.master.domain.player.dto.sign;

import org.codequistify.master.domain.player.domain.Player;

public record PlayerDTO(
        Long id,
        String email,
        String name,
        String oAuthType,
        String oAuthId,
        Integer level
) {
    public PlayerDTO of(Long id, String email, String name, String oAuthType, String oAuthId, Integer level) {
        return new PlayerDTO(id, email, name, oAuthType, oAuthId, level);
    }

    public Player toPlayer(String password) {
        return Player.builder()
                .email(this.email)
                .name(this.name)
                .oAuthType(this.oAuthType)
                .oAuthId(this.oAuthId)
                .level(this.level)
                .password(password)
                .build();
    }
}
