package org.codequistify.master.domain.player.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.domain.player.dto.details.PlayerInfoResponse;
import org.codequistify.master.domain.player.dto.sign.PlayerDTO;
import org.codequistify.master.domain.player.dto.sign.SignInResponse;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Player extends BaseTimeEntity implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "oauth_type")
    @Enumerated(EnumType.STRING)
    private OAuthType oAuthType;

    @Column(name = "oauth_id")
    private String oAuthId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Column(name = "locked")
    private Boolean isLocked;

    // 수정 많음 테이블 분할 필요

    @Column(name = "oauth_access_token")
    private String oAuthAccessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "level")
    private Integer level;

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

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
}
