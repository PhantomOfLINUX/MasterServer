package org.codequistify.master.core.domain.player.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.codequistify.master.core.domain.player.domain.OAuthType;
import org.codequistify.master.core.domain.player.domain.PlayerAccessType;
import org.codequistify.master.core.domain.player.domain.PlayerRoleType;
import org.codequistify.master.core.domain.player.service.UidGenerator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Player {

    private final PolId uid;
    private final String name;
    private final String email;
    private final String password;
    private final OAuthType oAuthType;
    private final String oAuthId;
    private final Set<String> roles;
    private final Boolean locked;
    private final String oAuthAccessToken;
    private final String refreshToken;
    private final Integer exp;


    public Player withOAuthAccessToken(String token) {
        return this.toBuilder()
                   .oAuthAccessToken(token)
                   .build();
    }

    public Player clearOAuthAccessToken() {
        return this.toBuilder()
                   .oAuthAccessToken("")
                   .build();
    }

    public Player clearRefreshToken() {
        return this.toBuilder()
                   .refreshToken("")
                   .build();
    }

    public Player increaseLevelPoint(int point) {
        return this.toBuilder()
                   .exp(this.exp + point)
                   .build();
    }

    public Player addRoles(Set<PlayerRoleType> rolesToAdd, Set<PlayerAccessType> permissionsToAdd) {
        Set<String> merged = new HashSet<>(this.roles);
        rolesToAdd.forEach(roleType -> merged.add(roleType.getRole()));
        permissionsToAdd.forEach(playerAccessType -> merged.add(playerAccessType.getPermission()));
        return this.toBuilder()
                   .roles(Collections.unmodifiableSet(merged))
                   .build();
    }

    public static PolId generateUID(String email) {
        return UidGenerator.generate(email);
    }
}
