package org.codequistify.master.domain.stage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GradingRequest(
        @NotNull(message = "4101") Long playerId,
        @NotBlank(message = "4101") String stageCode,
        @NotNull(message = "4101") Integer questionIndex,
        String answer
) {
}
