package org.codequistify.master.core.domain.authentication.dto;

import org.codequistify.master.application.player.dto.PlayerProfile;
import org.codequistify.master.global.jwt.dto.TokenResponse;

public record LoginResponse(
        PlayerProfile profile,
        TokenResponse token
) {
}
