package org.codequistify.master.core.domain.player.converter;

import org.codequistify.master.core.domain.authentication.dto.SignUpRequest;
import org.codequistify.master.core.domain.player.domain.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.dto.PlayerProfile;
import org.springframework.stereotype.Component;

@Component
public class PlayerConverter {
    public PlayerProfile convert(Player player) {
        return new PlayerProfile(
                player.getUid(),
                player.getName(),
                player.getEmail(),
                player.getExp(),
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
                .locked(false)
                .exp(0)
                .build();
    }
}
