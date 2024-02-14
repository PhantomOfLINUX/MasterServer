package org.codequistify.master.domain.player.repository;

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

    Player getPlayerByUid(String uid);

    Optional<Player> findByUid(String uid);
}
