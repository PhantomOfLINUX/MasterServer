package org.codequistify.master.domain.authentication.dto;

import java.util.List;

public record LogInResponse(
        String uid,
        String name,
        String email,
        Integer level,
        List<String> roles
) {
}
