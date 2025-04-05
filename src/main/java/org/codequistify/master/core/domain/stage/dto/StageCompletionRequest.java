package org.codequistify.master.core.domain.stage.dto;

import org.codequistify.master.core.domain.stage.domain.CompletedStatus;

public record StageCompletionRequest(
        CompletedStatus status
) {
}
