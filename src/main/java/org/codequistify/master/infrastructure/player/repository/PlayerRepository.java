package org.codequistify.master.infrastructure.player.repository;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.infrastructure.player.converter.PlayerConverter;
import org.codequistify.master.infrastructure.player.entity.PlayerEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PlayerRepository {

    private final PlayerJpaRepository jpaRepository;

    public Player save(Player player) {
        PlayerEntity entity = PlayerConverter.toEntity(player);
        PlayerEntity saved = jpaRepository.save(entity);
        return PlayerConverter.toDomain(saved);
    }

    public Optional<Player> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(PlayerConverter::toDomain);
    }

    public Optional<Player> findByUid(String uid) {
        return jpaRepository.findByUid(uid).map(PlayerConverter::toDomain);
    }

    public boolean existsByNameIgnoreCase(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    public boolean existsByEmailIgnoreCase(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    public void updateRefreshToken(String uid, String refreshToken) {
        jpaRepository.updateRefreshToken(uid, refreshToken);
    }

    public String getRefreshToken(String uid) {
        return jpaRepository.getRefreshToken(uid);
    }

    public Optional<OAuthType> getOAuthTypeByEmail(String email) {
        return jpaRepository.getOAuthTypeByEmail(email);
    }
}