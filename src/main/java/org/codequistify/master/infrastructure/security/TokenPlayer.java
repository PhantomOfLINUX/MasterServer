package org.codequistify.master.infrastructure.security;

import lombok.Builder;
import lombok.Getter;
import org.codequistify.master.core.domain.player.model.PolId;

import java.util.Set;

@Getter
@Builder
public class TokenPlayer {
    private final String email;
    private final String name;
    private final Set<String> roles;
    private final PolId uid;
}
