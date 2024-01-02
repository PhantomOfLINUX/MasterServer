package org.codequistify.master.domain.player.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Column(name = "auth_type")
    private String authType;

    @Column(name = "auth_id")
    private Long authId;

    @Column(name = "level")
    private Integer level;

    public void encodePassword(){
        this.password = new BCryptPasswordEncoder().encode(this.password);
    }

    public boolean decodePassword(String password){
        return new BCryptPasswordEncoder().matches(password, this.password);
    }

    public PlayerDTO toPlayerDTO(){
        return new PlayerDTO(this.id, this.email, this.name, this.authType, this.authId, this.level);
    }

    @Builder
    public Player(String name, String email, String password, String authType, Long authId, Integer level) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.authType = authType;
        this.authId = authId;
        this.level = level;
    }

    public Player() {
    }
}
