package org.codequistify.master.global.data;

import java.util.Objects;

public record Pair<F, S>(F first, S second) {
    public Pair {
        Objects.requireNonNull(first, "first must not be null");
        Objects.requireNonNull(second, "second must not be null");
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }
}
