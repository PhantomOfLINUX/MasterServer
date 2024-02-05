package org.codequistify.master.domain.player.dto.sign;

public record LogInResponse(
        String uid,
        String name,
        String email,
        Integer level
) {
}
