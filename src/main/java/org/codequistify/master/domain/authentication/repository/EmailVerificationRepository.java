package org.codequistify.master.domain.authentication.repository;

import org.codequistify.master.domain.authentication.domain.EmailVerification;
import org.codequistify.master.domain.authentication.domain.EmailVerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findFirstByEmailAndUsedAndEmailVerificationTypeOrderByCreatedDateDesc(String email, Boolean used, EmailVerificationType type);
}
