package org.codequistify.master.core.domain.player.service;

import org.codequistify.master.core.domain.player.model.Player;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PlayerPasswordService {
    private final PasswordEncoder encoder;

    public PlayerPasswordService(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public Player encodePassword(Player player) {
        return player.toBuilder()
                     .password(encoder.encode(player.getPassword()))
                     .build();
    }

    public Player encodePassword(Player player, String rawPassword) {
        return player.toBuilder()
                     .password(encoder.encode(rawPassword))
                     .build();
    }

    public boolean matches(Player player, String rawPassword) {
        return encoder.matches(rawPassword, player.getPassword());
    }
}
