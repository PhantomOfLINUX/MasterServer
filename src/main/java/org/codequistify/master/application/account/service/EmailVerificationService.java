package org.codequistify.master.application.account.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.strategy.EmailVerificationPolicyHandler;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.core.domain.account.model.EmailVerification;
import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.codequistify.master.core.domain.account.service.EmailVerificationCodeManager;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.infrastructure.account.converter.EmailVerificationConverter;
import org.codequistify.master.infrastructure.account.mail.EmailMessageFactory;
import org.codequistify.master.infrastructure.account.mail.EmailSender;
import org.codequistify.master.infrastructure.account.repository.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationCodeManager codeManager;
    private final EmailMessageFactory emailMessageFactory;
    private final EmailSender emailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    private final EmailVerificationPolicyHandler policyHandler;
    @Value("${mail.secret}")
    private String mailSecret;

    @Async
    @Transactional
    public void validateAndSend(String email, EmailVerificationType type) {
        policyHandler.validate(email, type);
        sendVerifyMail(email, type);
    }

    @Async
    @Transactional
    public void sendVerifyMail(String email, EmailVerificationType type) {
        try {
            String code = codeManager.generate(email);
            MimeMessage message = emailMessageFactory.createVerificationMessage(email, code, type);
            emailSender.send(message);

            emailVerificationRepository.save(EmailVerification.of(email, code, type));

            logger.info("[sendVerifyMail] {}로 메일 전송 및 인증 정보 저장 완료", email);
        } catch (Exception e) {
            logger.error("[sendVerifyMail] 실패 - {}", email);
            throw new ApplicationException(ErrorCode.EMAIL_SENDING_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @LogMonitoring
    public boolean verifyCode(String email, String code) {
        return codeManager.matches(email, code);
    }

    @Transactional
    public void updateVerification(String email, EmailVerificationType type) {
        emailVerificationRepository.findLatestUnusedVerification(email, false, type)
                                   .map(EmailVerificationConverter::toDomain)
                                   .map(EmailVerification::markAsVerified)
                                   .map(emailVerificationRepository::save)
                                   .ifPresentOrElse(
                                           v -> logger.info("[EmailVerification] 인증 완료 - email={}, type={}",
                                                            email,
                                                            type),
                                           () -> {
                                               logger.warn("[EmailVerification] 인증 정보 없음 - email={}, type={}",
                                                           email,
                                                           type);
                                               throw new ApplicationException(ErrorCode.EMAIL_VERIFIED_FAILURE,
                                                                              HttpStatus.BAD_REQUEST);
                                           }
                                   );
    }

    @Transactional
    public EmailVerification verifyEmailAndRetrieve(String email, EmailVerificationType type) {
        EmailVerification verification = getEmailVerificationByEmail(email, false, type);

        if (!verification.isVerified()) {
            logger.warn("[EmailVerification] 인증되지 않은 상태 - email={}, type={}", email, type);
            throw new ApplicationException(ErrorCode.EMAIL_VERIFIED_FAILURE, HttpStatus.BAD_REQUEST);
        }

        return verification;
    }

    @Transactional
    public EmailVerification getEmailVerificationByEmail(String email,
                                                         Boolean used,
                                                         EmailVerificationType type) {
        return emailVerificationRepository.findLatestUnusedVerification(email, used, type)
                                          .map(EmailVerificationConverter::toDomain)
                                          .orElseThrow(() -> {
                                              logger.warn("[EmailVerification] 인증 정보 조회 실패 - email={}, used={}, type={}",
                                                          email,
                                                          used,
                                                          type);
                                              return new ApplicationException(ErrorCode.EMAIL_VERIFIED_FAILURE,
                                                                              HttpStatus.BAD_REQUEST);
                                          });
    }


    @Transactional
    public void markEmailVerificationAsUsed(EmailVerification verification) {
        emailVerificationRepository.save(verification.markAsUsed());
        logger.info("[markEmailVerificationAsUsed] 사용 처리 완료 - {}", verification.getEmail());
    }

    private String generateCode(String email) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update((email + mailSecret).getBytes(StandardCharsets.UTF_8));
        return String.format("%064x", new BigInteger(1, md.digest()));
    }
}
