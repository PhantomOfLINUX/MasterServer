package org.codequistify.master.domain.player.dto;

public record PlayerDTO(
        Long id,
        String email,
        String name,
        String authType,
        Long authId,
        Integer level
) {
    public PlayerDTO of(Long id, String email, String name, String authType, Long authId, Integer level){
        return new PlayerDTO(id, email, name, authType, authId, level);
    }
}
