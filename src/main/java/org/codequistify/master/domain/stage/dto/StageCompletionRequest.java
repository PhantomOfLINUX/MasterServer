package org.codequistify.master.domain.stage.dto;

import org.codequistify.master.domain.stage.domain.CompletedStatus;

public record StageCompletionRequest(
        CompletedStatus status
)
{
}
