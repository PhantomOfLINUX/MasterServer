package org.codequistify.master.core.domain.player.converter;

import org.codequistify.master.application.account.dto.SignUpRequest;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.application.player.dto.PlayerProfile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlayerConverter {
    public PlayerProfile convert(Player player) {
        return new PlayerProfile(
                player.getUid().getValue(),
                player.getName(),
                player.getEmail(),
                player.getExp(),
                List.copyOf(player.getRoles())
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
