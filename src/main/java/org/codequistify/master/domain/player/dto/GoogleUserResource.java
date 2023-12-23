package org.codequistify.master.domain.player.dto;

public record GoogleUserResource(
        String id,
        String email,
        String name
) {
    public GoogleUserResource of(String id, String email, String name){
        return new GoogleUserResource(id, email, name);
    }
}
