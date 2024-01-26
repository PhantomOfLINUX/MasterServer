package org.codequistify.master.domain.player.domain.repository;

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
    @Query("UPDATE Player p SET p.refreshToken = :refreshToken WHERE p.id = :id")
    void updateRefreshToken(Long id, String refreshToken);
}
