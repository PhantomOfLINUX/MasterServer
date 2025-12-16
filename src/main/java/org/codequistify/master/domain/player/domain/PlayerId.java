package org.codequistify.master.domain.player.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public record PlayerId(Long value) {
    public PlayerId {
        Objects.requireNonNull(value, "player id must not be null");
    }

    public static PlayerId of(Long value) {
        return new PlayerId(value);
    }
}
