package org.codequistify.master.infrastructure.authentication.repository;

import org.codequistify.master.core.domain.authentication.model.EmailVerification;
import org.codequistify.master.core.domain.authentication.model.EmailVerificationType;
import org.codequistify.master.infrastructure.authentication.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findFirstByEmailAndUsedAndEmailVerificationTypeOrderByCreatedDateDesc(String email,
                                                                                                      Boolean used,
                                                                                                      EmailVerificationType type);

    @Query("""
                SELECT e
                FROM EmailVerificationEntity e
                WHERE e.email = :email
                  AND e.used = :used
                  AND e.emailVerificationType = :type
                ORDER BY e.createdDate DESC
                LIMIT 1
            """)
    Optional<EmailVerificationEntity> findLatestUnusedVerification(String email, Boolean used, EmailVerificationType type);
}
