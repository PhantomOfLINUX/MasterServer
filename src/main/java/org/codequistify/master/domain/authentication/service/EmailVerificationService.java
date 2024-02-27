package org.codequistify.master.domain.authentication.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.domain.EmailVerification;
import org.codequistify.master.domain.authentication.repository.EmailVerificationRepository;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    @Value("${mail.secret}")
    private String mailSecret;

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationService.class);

    @Async
    public void sendVerifyMail(String email) throws MessagingException {
        String authCode;
        try {
            authCode = generatedCode(email);

            Map<String, Object> variables = new HashMap<>();
            variables.put("authCode", authCode);
            variables.put("email", URLEncoder.encode(email, StandardCharsets.UTF_8));

            MimeMessage message = createMessage(email, variables);
            javaMailSender.send(message);
            LOGGER.info("[sendVerifyMail] {}로 메일 전송 완료", email);

            this.save(email, authCode);

        } catch (NoSuchAlgorithmException | MailException exception) {
            LOGGER.info("[sendVerifyMail] {}로 메일 전송 실패", email);
            throw new BusinessException(ErrorCode.EMAIL_SENDING_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LogMonitoring
    public boolean verifyCode(String email, String code) {
        try {
            return generatedCode(email).equals(code);
        } catch (NoSuchAlgorithmException exception) {
            return false;
        }
    }

    @LogMonitoring
    public void updateVerification(String email) {
        emailVerificationRepository.findByEmail(email)
                .ifPresent(emailVerification -> {
                    emailVerification.verify();
                    emailVerificationRepository.save(emailVerification);
                });
    }

    private EmailVerification save(String email, String code) {
        EmailVerification emailVerification = new EmailVerification(email, code, false);
        return emailVerificationRepository.save(emailVerification);
    }

    /*
    인증 코드 발급
     */
    @LogMonitoring
    private String generatedCode(String email) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update((email + mailSecret).getBytes(StandardCharsets.UTF_8));

        return String.format("%064x", new BigInteger(1, messageDigest.digest()));
    }

    /*
    템플릿으로 메일 본문 만들기
     */
    @LogMonitoring
    private MimeMessage createMessage(String toMail, Map<String, Object> variables) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, toMail);
        message.setFrom("kkwjdfo@gmail.com");
        message.setSubject("인증 메일 : POL 회원가입");

        Context context = new Context();
        context.setVariables(variables);
        LOGGER.info("[createMessage] auth code {}", variables.get("authCode"));
        String htmlContent = templateEngine.process("/mail/email-verification", context);

        message.setText(htmlContent, "utf-8", "html");

        LOGGER.info("[createMessage] 메일 생성");
        return message;
    }

    @LogMonitoring
    public EmailVerification getEmailVerificationByEmail(String email) {
        return emailVerificationRepository.findByEmail(email)
                .orElseThrow(()->{
                    return new BusinessException(ErrorCode.EMAIL_VERIFIED_FAILURE, HttpStatus.BAD_REQUEST);
                });
    }
}
