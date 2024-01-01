package org.codequistify.master.domain.player.dto;

import org.codequistify.master.domain.player.domain.Player;

public record SignRequest(
        String code,
        String name,
        String email,
        String password
) {
    public Player toPlayer(){
        return Player.builder()
                .name(name)
                .email(email)
                .password(password)
                .authId(0L)
                .authType("pol")
                .level(0).build();
    }
}
