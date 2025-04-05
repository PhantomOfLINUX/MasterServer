package org.codequistify.master.infrastructure.security;

import lombok.Getter;
import org.codequistify.master.core.domain.player.model.Player;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class PlayerUserDetails implements UserDetails {

    private final Player player;

    public PlayerUserDetails(Player player) {
        this.player = player;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return player.getRoles().stream()
                     .map(SimpleGrantedAuthority::new)
                     .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return player.getPassword();
    }

    @Override
    public String getUsername() {
        return player.getUid().getValue(); // PolId
    }

    @Override
    public boolean isAccountNonExpired() {
        return !player.getLocked();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !player.getLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !player.getLocked();
    }

    @Override
    public boolean isEnabled() {
        return !player.getLocked();
    }
}