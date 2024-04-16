package org.codequistify.master.domain.authentication.repository;

import org.codequistify.master.domain.authentication.domain.GoogleOAuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleOAuthUserRepository extends JpaRepository<GoogleOAuthUser, String> {
}
