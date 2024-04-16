package org.codequistify.master.domain.authentication.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.global.util.BaseTimeEntity;

@ToString
@Getter
@Entity
@Table(name = "google_oauth_user")
public class GoogleOAuthUser extends BaseTimeEntity {
    @Id
    private String id;

    private String email;

    private String name;

    @OneToOne(mappedBy = "googleOAuthUser")
    private PlayerAuthentication playerAuthentication;

    @Builder
    public GoogleOAuthUser(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public GoogleOAuthUser() {
    }
}
