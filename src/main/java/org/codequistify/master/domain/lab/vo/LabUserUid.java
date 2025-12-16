package org.codequistify.master.domain.lab.vo;

import org.codequistify.master.domain.player.domain.Player;

import java.util.Locale;
import java.util.Objects;

public record LabUserUid(String value) {
    public LabUserUid {
        Objects.requireNonNull(value, "uid must not be null");
        value = value.toLowerCase(Locale.ROOT);
    }

    public static LabUserUid from(Player player) {
        return new LabUserUid(player.getUid());
    }

    public static LabUserUid from(String value) {
        return new LabUserUid(value);
    }
}

