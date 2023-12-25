package org.codequistify.master.domain.player.dto;

public record SignRequest(
        String code,
        String email,
        String password
) {
}
