package org.codequistify.master.domain.authentication.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.global.util.BaseTimeEntity;

@ToString
@Getter
@Entity
@Table(name = "player_auth")
public class PlayerAuthentication extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @NotNull
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "google_oauth_user_id")
    private GoogleOAuthUser googleOAuthUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "github_oauth_user_id")
    private GithubOAuthUser githubOAuthUser;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "player_id")
    private Player player;

    public void linkGoogleOAuthUser(GoogleOAuthUser googleOAuthUser) {
        this.googleOAuthUser = googleOAuthUser;
    }

    public void linkGithubOAuthUser(GithubOAuthUser githubOAuthUser) {
        this.githubOAuthUser = githubOAuthUser;
    }

    public void linkPlayer(Player player) {
        this.player = player;
    }

    @Builder
    public PlayerAuthentication(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public PlayerAuthentication() {
    }
}
