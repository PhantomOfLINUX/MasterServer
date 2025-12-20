package org.codequistify.master.domain.authentication.repository;

import java.util.Optional;
import org.codequistify.master.domain.authentication.domain.EmailVerification;
import org.codequistify.master.domain.authentication.domain.EmailVerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
  Optional<EmailVerification> findFirstByEmailAndUsedAndEmailVerificationTypeOrderByCreatedDateDesc(
      String email, Boolean used, EmailVerificationType type);
}
