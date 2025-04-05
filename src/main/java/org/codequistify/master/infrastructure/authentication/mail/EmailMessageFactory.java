package org.codequistify.master.infrastructure.authentication.mail;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.authentication.model.EmailVerificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailMessageFactory {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final Logger logger = LoggerFactory.getLogger(EmailMessageFactory.class);

    public MimeMessage createVerificationMessage(String email, String code, EmailVerificationType type) throws
                                                                                                        MessagingException {
        Map<String, Object> variables = Map.of(
                "authCode", code,
                "email", URLEncoder.encode(email, StandardCharsets.UTF_8),
                "type", type.name()
        );

        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process("email-verification", context);

        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom("kkwjdfo@gmail.com");
        message.addRecipients(Message.RecipientType.TO, email);
        message.setSubject(getSubject(type));
        message.setText(html, "utf-8", "html");

        logger.info("[EmailMessageFactory] 메일 생성 완료 - authCode: {}", code);
        return message;
    }

    private String getSubject(EmailVerificationType type) {
        return switch (type) {
            case REGISTRATION -> "인증 메일 : POL 회원가입";
            case PASSWORD_RESET -> "인증 메일 : 비밀번호 초기화";
        };
    }
}