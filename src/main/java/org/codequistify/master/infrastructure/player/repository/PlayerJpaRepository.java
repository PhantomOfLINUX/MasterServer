package org.codequistify.master.infrastructure.player.repository;

import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.infrastructure.player.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PlayerJpaRepository extends JpaRepository<PlayerEntity, String> {
    Optional<PlayerEntity> findByEmail(String email);

    Optional<PlayerEntity> findByUid(String uid);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByEmailIgnoreCase(String email);

    @Modifying
    @Transactional
    @Query("UPDATE PlayerEntity p SET p.refreshToken = :refreshToken WHERE p.uid = :uid")
    void updateRefreshToken(String uid, String refreshToken);

    @Transactional(readOnly = true)
    @Query("SELECT p.refreshToken FROM PlayerEntity p WHERE p.uid = :uid")
    String getRefreshToken(String uid);

    @Transactional(readOnly = true)
    @Query("SELECT p.oAuthType FROM PlayerEntity p WHERE p.email = :email")
    Optional<OAuthType> getOAuthTypeByEmail(String email);
}