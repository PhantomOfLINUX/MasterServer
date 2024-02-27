package org.codequistify.master.domain.authentication.dto;

import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.global.jwt.dto.TokenResponse;

public record LoginResponse(
        PlayerProfile profile,
        TokenResponse token
) {
}
