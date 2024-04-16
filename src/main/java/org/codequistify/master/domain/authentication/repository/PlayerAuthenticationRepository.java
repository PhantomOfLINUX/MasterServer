package org.codequistify.master.domain.authentication.repository;

import org.codequistify.master.domain.authentication.domain.PlayerAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerAuthenticationRepository extends JpaRepository<PlayerAuthentication, Long> {
}
