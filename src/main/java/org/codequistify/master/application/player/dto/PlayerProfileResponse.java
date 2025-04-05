package org.codequistify.master.application.player.dto;

import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.vo.Email;

import java.util.Set;

public record PlayerProfileResponse(
        PolId uid,
        String name,
        Email email,
        Integer level,
        Set<String> roles
) {

    public static PlayerProfileResponse of(
            PolId uid,
            String name,
            Email email,
            Integer level,
            Set<String> roles
    ) {
        return new PlayerProfileResponse(
                uid,
                name,
                email,
                level,
                roles
        );
    }

    public static PlayerProfileResponse from(Player player) {
        return new PlayerProfileResponse(
                player.getUid(),
                player.getName(),
                player.getEmail(),
                player.getExp(),
                player.getRoles()
        );
    }
}
