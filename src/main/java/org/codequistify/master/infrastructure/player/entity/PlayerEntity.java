package org.codequistify.master.infrastructure.player.entity;

import jakarta.persistence.*;
import lombok.*;
import org.codequistify.master.core.domain.player.model.OAuthType;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.global.util.BaseTimeEntity;
import org.codequistify.master.infrastructure.player.converter.PolIdConverter;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player", indexes = {
        @Index(name = "idx_uid", columnList = "uid", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlayerEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long id;

    @Column(name = "uid", unique = true)
    @Convert(converter = PolIdConverter.class)
    private PolId uid;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "oauth_type")
    @Enumerated(EnumType.STRING)
    @ColumnDefault("POL")
    private OAuthType oAuthType;

    @Column(name = "oauth_id")
    private String oAuthId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Column(name = "locked")
    @ColumnDefault("false")
    private Boolean locked;

    @Column(name = "oauth_access_token")
    private String oAuthAccessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "exp")
    @ColumnDefault("0")
    private int exp;
}
