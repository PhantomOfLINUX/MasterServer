package org.codequistify.master.infrastructure.player.converter;

import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.infrastructure.player.entity.PlayerEntity;

import java.util.HashSet;

public class PlayerConverter {

    public static Player toDomain(PlayerEntity entity) {
        if (entity == null) return null;

        return Player.builder()
                     .uid(PolId.of(entity.getUid()))
                     .name(entity.getName())
                     .email(entity.getEmail())
                     .password(entity.getPassword())
                     .oAuthType(entity.getOAuthType())
                     .oAuthId(entity.getOAuthId())
                     .roles(new HashSet<>(entity.getRoles()))
                     .locked(entity.getLocked())
                     .oAuthAccessToken(entity.getOAuthAccessToken())
                     .refreshToken(entity.getRefreshToken())
                     .exp(entity.getExp())
                     .build();
    }

    public static PlayerEntity toEntity(Player domain) {
        if (domain == null) return null;

        return PlayerEntity.builder()
                           .uid(domain.getUid().getValue())
                           .name(domain.getName())
                           .email(domain.getEmail())
                           .password(domain.getPassword())
                           .oAuthType(domain.getOAuthType())
                           .oAuthId(domain.getOAuthId())
                           .roles(domain.getRoles().stream().toList())
                           .locked(domain.getLocked())
                           .oAuthAccessToken(domain.getOAuthAccessToken())
                           .refreshToken(domain.getRefreshToken())
                           .exp(domain.getExp())
                           .build();
    }
}