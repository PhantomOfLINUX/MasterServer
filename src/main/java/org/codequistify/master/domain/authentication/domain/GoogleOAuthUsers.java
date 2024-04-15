package org.codequistify.master.domain.authentication.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.global.util.BaseTimeEntity;

@ToString
@Getter
@Entity
public class GoogleOAuthUsers extends BaseTimeEntity {
    @Id
    private String id;

    private String email;

    private String name;

    @Builder
    public GoogleOAuthUsers(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public GoogleOAuthUsers() {
    }
}
