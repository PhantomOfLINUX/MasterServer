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
@Table(name = "github_oauth_user")
public class GithubOAuthUser extends BaseTimeEntity {
    @Id
    private String id;

    private String name;

    @OneToOne(mappedBy = "githubOAuthUser")
    private PlayerAuthentication playerAuthentication;

    @Builder
    public GithubOAuthUser(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public GithubOAuthUser() {
    }
}
