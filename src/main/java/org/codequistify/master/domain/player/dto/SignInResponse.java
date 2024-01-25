package org.codequistify.master.domain.player.dto;

public record SignInResponse(
        Long id,
        String email,
        String name,
        Integer level
) {
}
