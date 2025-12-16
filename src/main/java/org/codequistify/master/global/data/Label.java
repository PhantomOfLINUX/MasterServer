package org.codequistify.master.global.data;

import java.util.List;
import java.util.Objects;

public record Label(String key, List<String> values) {
    public Label {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(values, "values must not be null");
        values = List.copyOf(values);
    }

    public static Label of(String key, String... values) {
        return new Label(key, List.of(values));
    }

    public static Label of(String key, List<String> values) {
        return new Label(key, values);
    }

    public String firstValue() {
        return values.stream().findFirst().orElse("");
    }

    public Pair<String, String> firstValuePair() {
        return Pair.of(key, firstValue());
    }
}
