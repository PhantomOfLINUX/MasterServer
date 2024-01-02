package org.codequistify.master.domain.player.service.impl;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerifyMailService {

    @Value("${mail.secret}")
    private String mailSecret;

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;
    private final Logger LOGGER = LoggerFactory.getLogger(VerifyMailService.class);

    @Async
    public void sendVerifyMail(String email) throws MessagingException {
        String authCode;
        try {
            authCode = generatedCode(email);
        }catch (NoSuchAlgorithmException exception){
            LOGGER.info("[sendVerifyMail] {}로 메일 전송 실패", email);
            throw new MailSendException("인증 메일 전송 중 오류 발생");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("authCode", authCode);
        variables.put("email", email);

        MimeMessage message = createMessage(email, variables);
        try {
            javaMailSender.send(message);
        } catch (MailException exception) {
            LOGGER.info("[sendVerifyMail] {}로 메일 전송 실패", email);
            throw new MailSendException("인증 메일 전송 중 오류 발생");
        }
        LOGGER.info("[sendVerifyMail] {}로 메일 전송 완료", email);
    }

    public boolean checkValidCode(String email, String code){
        try {
            return generatedCode(email).equals(code);
        }catch (NoSuchAlgorithmException exception){
            return false;
        }
    }

    private String generatedCode(String email) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update((email + mailSecret).getBytes(StandardCharsets.UTF_8));
        return String.format("%064x", new BigInteger(1, messageDigest.digest())).substring(0, 8);
    }

    private MimeMessage createMessage(String toMail, Map<String, Object> variables) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(Message.RecipientType.TO, toMail);
        message.setFrom("kkwjdfo@gmail.com");
        message.setSubject("인증 메일 : POL 회원가입");

        Context context = new Context();
        context.setVariables(variables);
        LOGGER.info("[createMessage] auth code {}", variables.get("authCode"));
        String htmlContent = templateEngine.process("VerifyEmailTemplate", context);

        message.setText(htmlContent, "utf-8", "html");

        LOGGER.info("[createMessage] 메일 생성");
        return message;
    }
}
