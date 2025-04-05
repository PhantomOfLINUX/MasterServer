package org.codequistify.master.infrastructure.security;

import org.codequistify.master.core.domain.player.model.Player;

public class PlayerUserDetailsFactory {

    public static PlayerUserDetails from(Player player) {
        return new PlayerUserDetails(player);
    }
}