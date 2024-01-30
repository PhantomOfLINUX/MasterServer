package org.codequistify.master.domain.player.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.domain.player.dto.details.PlayerInfoResponse;
import org.codequistify.master.domain.player.dto.sign.PlayerDTO;
import org.codequistify.master.domain.player.dto.sign.SignInResponse;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Getter
@ToString
public class Player extends BaseTimeEntity {
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

    // 비밀번호 암호화
    public void encodePassword(){
        this.password = new BCryptPasswordEncoder().encode(this.password);
    }
    public void encodePassword(String password){
        this.password = new BCryptPasswordEncoder().encode(password);
    }

    // 비밀번호 일치 판정
    public boolean decodePassword(String password){
        return new BCryptPasswordEncoder().matches(password, this.password);
    }

    // OAuth 발급 AccessToken 설정
    public void updateOAuthAccessToken(String oAuthAccessToken) {
        this.oAuthAccessToken = oAuthAccessToken;
    }

    // OAuth 발급 AccessToken 초기화
    public void clearOAuthAccessToken() {
        this.oAuthAccessToken = "";
    }

    // OAuth 발급 RefreshToken 초기화
    public void clearRefreshToken() {
        this.refreshToken = "";
    }

    public int increaseLevelPoint(int point) {
        this.level += point;
        return this.level;
    }


    public PlayerDTO toPlayerDTO(){
        return new PlayerDTO(this.id, this.email, this.name, this.oAuthType, this.oAuthId, this.level);
    }

    public SignInResponse toSignInResponse() {
        return new SignInResponse(this.id, this.email, this.name, this.level);
    }

    public PlayerInfoResponse toPlayerInfoResponse() {
        return new PlayerInfoResponse(this.id, this.email, this.name, this.level);
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
