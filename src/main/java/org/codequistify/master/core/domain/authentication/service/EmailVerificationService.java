package org.codequistify.master.core.domain.authentication.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.authentication.domain.EmailVerification;
import org.codequistify.master.core.domain.authentication.domain.EmailVerificationType;
import org.codequistify.master.core.domain.authentication.repository.EmailVerificationRepository;
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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public void sendVerifyMail(String email, EmailVerificationType emailVerificationType) throws MessagingException {
        String authCode;
        try {
            authCode = generatedCode(email);

            Map<String, Object> variables = new HashMap<>();
            variables.put("authCode", authCode);
            variables.put("email", URLEncoder.encode(email, StandardCharsets.UTF_8));
            variables.put("type", emailVerificationType.name());

            MimeMessage message = createMessage(email, variables, emailVerificationType);
            javaMailSender.send(message);
            LOGGER.info("[sendVerifyMail] {}로 메일 전송 완료", email);

            this.saveEmailVerification(email, authCode, emailVerificationType);

        } catch (NoSuchAlgorithmException | MailException exception) {
            LOGGER.info("[sendVerifyMail] {}로 메일 전송 실패", email);
            throw new BusinessException(ErrorCode.EMAIL_SENDING_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
    템플릿으로 메일 본문 만들기
     */
    @LogMonitoring
    private MimeMessage createMessage(String toMail, Map<String, Object> variables, EmailVerificationType type) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, toMail);
        message.setFrom("kkwjdfo@gmail.com");
        if (type == EmailVerificationType.REGISTRATION) {
            message.setSubject("인증 메일 : POL 회원가입");
        }
        if (type == EmailVerificationType.PASSWORD_RESET) {
            message.setSubject("인증 메일 : 비밀번호 초기화");
        }

        Context context = new Context();
        context.setVariables(variables);
        LOGGER.info("[createMessage] auth code {}", variables.get("authCode"));

        String template = getEmailTemplateName(type);
        String htmlContent = templateEngine.process(template, context);

        message.setText(htmlContent, "utf-8", "html");

        LOGGER.info("[createMessage] 메일 생성");
        return message;
    }

    // 인증 정보 틀 저장
    @Transactional
    public EmailVerification saveEmailVerification(String email, String code, EmailVerificationType emailVerificationType) {
        EmailVerification emailVerification = new EmailVerification(email, code, emailVerificationType);
        return emailVerificationRepository.save(emailVerification);
    }
    private String getEmailTemplateName(EmailVerificationType emailVerificationType) {
        return "email-verification";
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

    // 인증번호 비교
    @LogMonitoring
    public boolean verifyCode(String email, String code) {
        try {
            return generatedCode(email).equals(code);
        } catch (NoSuchAlgorithmException exception) {
            return false;
        }
    }

    // 메일 인증번호 확인
    @LogMonitoring
    @Transactional
    public void updateVerification(String email, EmailVerificationType type) {
        emailVerificationRepository.findFirstByEmailAndUsedAndEmailVerificationTypeOrderByCreatedDateDesc(email, false, type) // 사용안된 인증 정보사용
                .ifPresent(emailVerification -> {
                    emailVerification.verify(); // 인증 표시
                    emailVerificationRepository.save(emailVerification);
                });
    }

    // 이메일 인증이 되어있는지 확인
    @Transactional
    public EmailVerification verifyEmailAndRetrieve(String email, EmailVerificationType type) {
        EmailVerification emailVerification = this.getEmailVerificationByEmail(email, false, type);

        // 이메일 인증이 되어 있는 계정인지 확인
        if (!emailVerification.getVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFIED_FAILURE, HttpStatus.BAD_REQUEST);
        }

        return emailVerification;
    }

    // 인증정보 가져오기
    @LogMonitoring
    @Transactional
    public EmailVerification getEmailVerificationByEmail(String email, Boolean used, EmailVerificationType type) {
        return emailVerificationRepository.findFirstByEmailAndUsedAndEmailVerificationTypeOrderByCreatedDateDesc(email, used, type)
                .orElseThrow(()->{
                    LOGGER.info("[getEmailVerificationByEmail] {}에 대한 미사용 메일 인증 정보 없음", email);
                    return new BusinessException(ErrorCode.EMAIL_VERIFIED_FAILURE, HttpStatus.BAD_REQUEST);
                });
    }

    // 인증 정보 사용 기록
    @LogMonitoring
    @Transactional
    public void markEmailVerificationAsUsed(EmailVerification emailVerification) {
        emailVerification.markAsUsed();
        emailVerificationRepository.save(emailVerification);
    }
}
