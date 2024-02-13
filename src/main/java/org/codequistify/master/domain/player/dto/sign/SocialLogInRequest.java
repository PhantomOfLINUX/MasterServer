package org.codequistify.master.domain.player.dto.sign;

import jakarta.validation.constraints.NotBlank;

public record SocialLogInRequest(
        @NotBlank String code
) {
}
