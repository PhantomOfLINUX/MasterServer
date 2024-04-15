package org.codequistify.master.domain.authentication.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Entity
public class GoogleOAuthUsers {
    @Id
    private String id;
}
