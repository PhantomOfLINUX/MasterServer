package org.codequistify.master.infrastructure.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.codequistify.master.core.domain.vo.Email;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.codequistify.master.infrastructure.converter.EmailConverter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@ToString
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "email_verification", indexes = {
        @Index(name = "idx_email", columnList = "email")
})
public class EmailVerificationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = EmailConverter.class)
    private Email email;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean verified = false;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EmailVerificationType emailVerificationType;

    @Builder
    public EmailVerificationEntity(Email email, String code, EmailVerificationType emailVerificationType) {
        this.email = email;
        this.code = code;
        this.emailVerificationType = emailVerificationType;
        this.verified = false;
        this.used = false;
    }
}
