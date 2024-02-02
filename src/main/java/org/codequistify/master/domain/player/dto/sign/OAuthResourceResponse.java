package org.codequistify.master.domain.player.dto.sign;

import java.util.Map;

public record OAuthResourceResponse(
        String id,
        String email,
        String name,
        Map<String, String> properties,
        Map<String, String> response
) {
}
