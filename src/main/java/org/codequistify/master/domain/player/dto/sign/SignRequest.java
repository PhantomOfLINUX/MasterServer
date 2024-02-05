package org.codequistify.master.domain.player.dto.sign;

public record SignRequest(
        String code,
        String name,
        String email,
        String password
) {
}
