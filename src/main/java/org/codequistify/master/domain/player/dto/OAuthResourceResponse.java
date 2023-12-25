package org.codequistify.master.domain.player.dto;

import java.util.Map;

public record OAuthResourceResponse(
        String id,
        String email,
        String name,
        Map<String, String> properties
) {
}
