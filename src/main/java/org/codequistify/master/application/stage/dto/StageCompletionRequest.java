package org.codequistify.master.application.stage.dto;

import org.codequistify.master.core.domain.stage.model.CompletedStatus;

public record StageCompletionRequest(
        CompletedStatus status
) {
}
