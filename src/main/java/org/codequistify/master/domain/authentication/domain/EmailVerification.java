package org.codequistify.master.domain.authentication.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;

@ToString
@Getter
@Entity
@Table(name = "email_verification", indexes = {
        @Index(name = "idx_email", columnList = "email")
})
public class EmailVerification extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String code;

    @Column(name = "verified")
    @ColumnDefault("false")
    private Boolean verified;

    @Column(name = "used")
    @ColumnDefault("false")
    private Boolean used;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private EmailVerificationType emailVerificationType;

    public void verify() {
        this.verified = true;
    }

    public void markAsUsed() {
        this.used = true;
    }

    @Builder
    public EmailVerification(String email, String code, EmailVerificationType emailVerificationType) {
        this.email = email;
        this.code = code;
        this.emailVerificationType = emailVerificationType;
        this.verified = false;
        this.used = false;
    }

    public EmailVerification() {
    }
}
