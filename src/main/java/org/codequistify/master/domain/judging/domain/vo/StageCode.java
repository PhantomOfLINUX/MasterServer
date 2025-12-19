package org.codequistify.master.domain.judging.domain.vo;

import org.codequistify.master.domain.stage.domain.Stage;
import org.codequistify.master.domain.stage.domain.StageImageType;

import java.util.Locale;
import java.util.Objects;

public record StageCode(String value) {
    public StageCode {
        Objects.requireNonNull(value, "stageCode must not be null");
    }

    public static StageCode from(Stage stage) {
        return from(stage.getStageImage());
    }

    public static StageCode from(StageImageType stageImageType) {
        return new StageCode(stageImageType.name());
    }

    public static StageCode from(String stageCode) {
        return new StageCode(stageCode);
    }

    public String lowercase() {
        return value.toLowerCase(Locale.ROOT);
    }
}
