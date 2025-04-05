package org.codequistify.master.application.account.dto;

import org.codequistify.master.application.player.dto.PlayerProfile;
import org.codequistify.master.global.jwt.dto.TokenResponse;

public record LoginResponse(
        PlayerProfile profile,
        TokenResponse token
) {
}
