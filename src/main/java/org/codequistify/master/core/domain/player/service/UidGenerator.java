package org.codequistify.master.core.domain.player.service;

import org.codequistify.master.core.domain.player.model.PolId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

public class UidGenerator {
    public static PolId generate() {
        return PolId.of(UUID.randomUUID().toString());
    }

    public static PolId generate(String email) {
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyMMddHH"));
        StringBuilder sb = new StringBuilder();

        sb.append("POL").append("-");

        // 년도 변환
        String year = formattedDate.substring(0, 2);
        for (char digit : year.toCharArray()) {
            if (digit == '0') {
                sb.append('0');
            } else {
                sb.append((char) ('A' + digit - '1'));
            }
        }

        // 월 변환
        String month = formattedDate.substring(2, 4);
        sb.append((char) ('A' + Integer.parseInt(month) - 1));

        // 일 변환
        String day = formattedDate.substring(4, 6);
        int dayInt = Integer.parseInt(day);
        if (dayInt <= 26) {
            char dayChar = (char) ('A' + dayInt - 1);
            sb.append(dayChar).append(Character.toLowerCase(dayChar));
        } else {
            sb.append("Z").append(dayInt - 26);
        }

        // 시간 변환
        String hour = formattedDate.substring(6, 8);
        sb.append((char) ('a' + Integer.parseInt(hour) - 1));

        // 이메일 변환
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(email.getBytes());

            sb.append("-").append(Base64.getEncoder().withoutPadding().encodeToString(md.digest()), 0, 10);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return PolId.of(sb.toString());
    }
}
