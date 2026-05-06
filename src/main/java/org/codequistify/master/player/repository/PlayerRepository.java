package org.codequistify.master.domain.player.repository;

import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.refreshToken = :refreshToken WHERE p.uid = :uid")
    void updateRefreshToken(String uid, String refreshToken);

    @Transactional
    @Query("SELECT p.refreshToken FROM Player p WHERE p.uid = :uid")
    String getRefreshToken(String uid);

    @Transactional
    @Query("SELECT p.oAuthType FROM Player p WHERE p.email = :email")
    Optional<OAuthType> getOAuthTypeByEmail(String email);

    Player getPlayerByUid(String uid);

    Optional<Player> findByUid(String uid);

    Boolean existsByNameIgnoreCase(String name);
}
