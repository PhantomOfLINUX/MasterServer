package org.codequistify.master.domain.player.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.domain.player.service.UidGenerator;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "player", indexes = {@Index(name = "idx_uid", columnList = "uid", unique = true)})
public class Player extends BaseTimeEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long id;

    @Column(name = "uid", unique = true)
    private String uid; // pol 고유 식별 번호

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "oauth_type") @ColumnDefault("pol")
    @Enumerated(EnumType.STRING)
    private OAuthType oAuthType;

    @Column(name = "oauth_id")
    private String oAuthId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Column(name = "locked") @ColumnDefault("false")
    private Boolean locked;

    // 수정 많음 테이블 분할 필요

    @Column(name = "oauth_access_token")
    private String oAuthAccessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "exp") @ColumnDefault("0")
    private Integer exp = 0;

    @Override
    public String getUsername() {
        return this.uid;
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
        return !locked;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !locked;
    }

    @Override
    public boolean isEnabled() {
        return !locked;
    }

    public void expireAccount() {
        this.locked = true;
    }

    public void dataClear() {
        this.name = null;
        this.email = null;
        this.password = null;
        this.oAuthId = null;
        this.roles.clear();
        this.locked = true;
        this.oAuthAccessToken = null;
        this.refreshToken = null;
    }

    // 비밀번호 암호화
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void encodePassword(String password, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    // 비밀번호 일치 판정
    public boolean decodePassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.password);
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
        this.exp += point;
        return this.exp;
    }

    public void addRoles(List<PlayerRoleType> roles, List<PlayerAccessType> permissions) {
        for (PlayerRoleType role : roles) {
            this.roles.add(role.getRole());
        }
        for (PlayerAccessType permission : permissions) {
            this.roles.add(permission.getPermission());
        }
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.email != null && !this.email.isEmpty()) {
            this.uid = generateUID();
        }
        addRoles(List.of(PlayerRoleType.PLAYER), List.of(PlayerAccessType.BASIC_PROBLEMS_ACCESS));
    }

    private String generateUID() {
        return UidGenerator.generate(this.email);
    }
}
