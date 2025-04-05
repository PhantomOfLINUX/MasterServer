package org.codequistify.master.infrastructure.security;

import org.codequistify.master.core.domain.player.model.Player;

public class PlayerDetailsFactory {

    public static PlayerDetails from(Player player) {
        return new PlayerDetails(player);
    }
}