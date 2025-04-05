package org.codequistify.master.infrastructure.account.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
    private final JavaMailSender javaMailSender;

    public void send(MimeMessage message) throws MessagingException {
        try {
            javaMailSender.send(message);
            logger.info("[EmailSender] 메일 전송 성공");
        } catch (MailException e) {
            logger.error("[EmailSender] 메일 전송 실패", e);
            throw e;
        }
    }
}
