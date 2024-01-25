package org.codequistify.master.domain.player.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.dto.SignInResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Getter
@ToString
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "oauth_type")
    private String oAuthType;

    @Column(name = "oauth_id")
    private String oAuthId;

    @Column(name = "oauth_access_token")
    private String oAuthAccessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "level")
    private Integer level;

    public void encodePassword(){
        this.password = new BCryptPasswordEncoder().encode(this.password);
    }

    public boolean decodePassword(String password){
        return new BCryptPasswordEncoder().matches(password, this.password);
    }

    public void updateOAuthAccessToken(String oAuthAccessToken) {
        this.oAuthAccessToken = oAuthAccessToken;
    }

    public PlayerDTO toPlayerDTO(){
        return new PlayerDTO(this.id, this.email, this.name, this.oAuthType, this.oAuthId, this.level);
    }

    public SignInResponse toSignInResponse() {
        return new SignInResponse(this.id, this.email, this.name, this.level);
    }

    @Builder
    public Player(String name, String email, String password, String oAuthType, String oAuthId, Integer level) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.oAuthType = oAuthType;
        this.oAuthId = oAuthId;
        this.level = level;
    }

    public Player() {
    }
}
