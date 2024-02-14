package org.codequistify.master.domain.player.dto.sign;

import java.util.List;

public record LogInResponse(
        String uid,
        String name,
        String email,
        Integer level,
        List<String> roles
) {
}
