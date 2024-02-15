package org.codequistify.master.domain.authentication.dto;

import org.codequistify.master.domain.player.dto.PlayerProfile;

public record LoginResponse(
        PlayerProfile profile,
        String accessToken
) {
}
