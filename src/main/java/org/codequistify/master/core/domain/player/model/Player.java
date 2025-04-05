package org.codequistify.master.core.domain.player.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.codequistify.master.core.domain.player.service.UidGenerator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Player {

    private final String email;
    private final Integer exp;
    private final Boolean locked;
    private final String name;
    private final String oAuthAccessToken;
    private final String oAuthId;
    private final OAuthType oAuthType;
    private final String password;
    private final String refreshToken;
    private final Set<String> roles;
    private final PolId uid;

    public static PolId generateUID(String email) {
        return UidGenerator.generate(email);
    }

    public String getId() {
        return this.uid.getValue();
    }

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

    /**
     * 계정 삭제 처리용 메서드.
     * 개인정보 및 인증정보 초기화, 잠금 처리
     */
    public Player dataClear() {
        return this.toBuilder()
                   .name(null)
                   .email(null)
                   .password(null)
                   .oAuthId(null)
                   .roles(Collections.emptySet())
                   .oAuthAccessToken(null)
                   .refreshToken(null)
                   .locked(true)
                   .build();
    }
}
