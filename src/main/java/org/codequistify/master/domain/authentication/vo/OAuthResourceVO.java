package org.codequistify.master.domain.authentication.vo;

import java.util.Map;

public record OAuthResourceVO(
        String id,
        String email,
        String name,
        Map<String, String> properties,
        Map<String, String> response
) {
}
