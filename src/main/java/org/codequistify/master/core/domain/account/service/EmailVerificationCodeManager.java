package org.codequistify.master.core.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.vo.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@Component
@RequiredArgsConstructor
public class EmailVerificationCodeManager {

    @Value("${mail.secret}")
    private String mailSecret;

    public String generate(Email email) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((email + mailSecret).getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, md.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    public boolean matches(Email email, String code) {
        return generate(email).equals(code);
    }
}