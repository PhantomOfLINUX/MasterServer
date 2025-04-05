package org.codequistify.master.application.account.dto;

import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.global.jwt.dto.TokenResponse;

public record LoginResponse(
        Player profile,
        TokenResponse token
) {
}
