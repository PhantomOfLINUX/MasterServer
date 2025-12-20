package org.codequistify.master.domain.shared.stage;

import java.util.Locale;
import java.util.Objects;

public record StageCode(String value) {
    public StageCode {
        Objects.requireNonNull(value, "stageCode must not be null");
        value = value.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("stageCode must not be blank");
        }
    }

    public static StageCode from(String value) {
        return new StageCode(value);
    }

    public String lowercase() {
        return value.toLowerCase(Locale.ROOT);
    }
}
