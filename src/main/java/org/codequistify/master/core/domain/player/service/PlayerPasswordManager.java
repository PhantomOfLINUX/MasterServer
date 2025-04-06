package org.codequistify.master.core.domain.player.service;

import org.codequistify.master.core.domain.player.model.Player;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PlayerPasswordManager {
    private final PasswordEncoder encoder;

    public PlayerPasswordManager(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(Player player, String rawPassword) {
        return encoder.matches(rawPassword, player.getPassword().getValue());
    }
}
