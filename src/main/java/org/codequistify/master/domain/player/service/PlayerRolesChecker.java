package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.player.domain.Player;

import java.util.HashSet;
import java.util.List;

public class PlayerRolesChecker {
    public static boolean checkRole(Player player, String role) {
        return player.getRoles().contains(role);
    }

    public static boolean checkEveryRole(Player player, List<String> roles) {
        for (String role : roles) {
            if (!player.getRoles().contains(role)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkAnyRole(Player player, List<String> roles) {
        for (String role : roles) {
            if (player.getRoles().contains(role)) {
                return true;
            }
        }
        return false;
    }


}
