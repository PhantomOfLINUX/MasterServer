package org.codequistify.master.domain.player.dto;

public record PlayerDTO(
        String id,
        String email,
        String name
) {
    public PlayerDTO of(String id, String email, String name){
        return new PlayerDTO(id, email, name);
    }
}
