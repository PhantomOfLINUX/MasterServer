package org.codequistify.master.domain.authentication.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@ToString
@Getter
@Entity
@Table(name = "email_verification", indexes = {
        @Index(name = "idx_email", columnList = "email")
})
public class EmailVerification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String email;

    String code;

    @ColumnDefault("false")
    Boolean verified;

    public void verify() {
        this.verified = true;
    }

    @Builder
    public EmailVerification(String email, String code, Boolean verified) {
        this.email = email;
        this.code = code;
        this.verified = verified;
    }

    public EmailVerification() {
    }
}
