package org.codequistify.master.domain.player.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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
    public void encodePassword() {
        this.password = new BCryptPasswordEncoder().encode(this.password);
    }

    public void encodePassword(String password) {
        this.password = new BCryptPasswordEncoder().encode(password);
    }

    // 비밀번호 일치 판정
    public boolean decodePassword(String password) {
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
        if (email != null && !email.isEmpty()) {
            this.uid = generateUID();
        }
        addRoles(Arrays.asList(PlayerRoleType.PLAYER), Arrays.asList(PlayerAccessType.BASIC_PROBLEMS_ACCESS));
    }

    private String generateUID() {
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyMMddHH"));
        StringBuilder sb = new StringBuilder();

        sb.append("POL").append("-");

        // 년도 변환
        String year = formattedDate.substring(0, 2);
        for (char digit : year.toCharArray()) {
            if (digit == '0') {
                sb.append('0');
            } else {
                sb.append((char) ('A' + digit - '1'));
            }
        }

        // 월 변환
        String month = formattedDate.substring(2, 4);
        sb.append((char) ('A' + Integer.parseInt(month) - 1));

        // 일 변환
        String day = formattedDate.substring(4, 6);
        int dayInt = Integer.parseInt(day);
        if (dayInt <= 26) {
            char dayChar = (char) ('A' + dayInt - 1);
            sb.append(dayChar).append(Character.toLowerCase(dayChar));
        } else {
            sb.append("Z").append(dayInt - 26);
        }

        // 시간 변환
        String hour = formattedDate.substring(6, 8);
        sb.append((char) ('a' + Integer.parseInt(hour) - 1));

        // 이메일 변환
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(email.getBytes());

            int r = new Random().nextInt(0, 3);
            sb.append("-").append(Base64.getEncoder().withoutPadding().encodeToString(md.digest()), r, r + 10);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }
}
