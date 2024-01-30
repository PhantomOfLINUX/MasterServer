package org.codequistify.master.domain.player.dto.sign;

public record SignInResponse(
        Long id,
        String email,
        String name,
        Integer level
) {
}
