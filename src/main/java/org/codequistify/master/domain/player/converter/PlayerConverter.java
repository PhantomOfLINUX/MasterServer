package org.codequistify.master.domain.player.converter;

import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.sign.LogInResponse;
import org.codequistify.master.domain.player.dto.sign.SignUpRequest;
import org.springframework.stereotype.Component;

@Component
public class PlayerConverter {
    public LogInResponse convert(Player player) {
        return new LogInResponse(
                player.getUid(),
                player.getName(),
                player.getEmail(),
                player.getLevel(),
                player.getRoles()
        );
    }

    public Player convert(SignUpRequest request) {
        return Player.builder()
                .email(request.email())
                .name(request.name())
                .password(request.password())
                .oAuthType(OAuthType.POL)
                .oAuthId("0")
                .isLocked(false)
                .level(0)
                .build();
    }
}
