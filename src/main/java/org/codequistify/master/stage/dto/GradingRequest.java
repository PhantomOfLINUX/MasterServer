package org.codequistify.master.domain.stage.dto;

import jakarta.validation.constraints.NotNull;

public record GradingRequest(
        @NotNull(message = "4101") Long stageId,
        @NotNull(message = "4101") Integer questionIndex,
        String answer
) {
}
