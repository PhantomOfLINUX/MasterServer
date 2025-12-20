package org.codequistify.master.virtualworkspace.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record VirtualWorkspaceCreateRequest(
        @NotBlank String stageCode
) {
}
