package org.codequistify.master.infrastructure.player.repository;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.vo.Email;
import org.codequistify.master.infrastructure.player.converter.PlayerConverter;
import org.codequistify.master.infrastructure.player.entity.PlayerEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    public Optional<Player> findByEmail(Email email) {
        return jpaRepository.findByEmail(email).map(PlayerConverter::toDomain);
    }

    public Optional<Player> findByUid(PolId uid) {
        return jpaRepository.findByUid(uid).map(PlayerConverter::toDomain);
    }

    public boolean existsByNameIgnoreCase(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }

    public boolean existsByEmailIgnoreCase(Email email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    public void updateRefreshToken(PolId uid, String refreshToken) {
        jpaRepository.updateRefreshToken(uid, refreshToken);
    }

    public String getRefreshToken(PolId uid) {
        return jpaRepository.getRefreshToken(uid);
    }

    public Optional<OAuthType> getOAuthTypeByEmail(Email email) {
        return jpaRepository.getOAuthTypeByEmail(email);
    }

    public List<Player> findAll() {
        return jpaRepository.findAll().stream().map(PlayerConverter::toDomain).toList();
    }
}