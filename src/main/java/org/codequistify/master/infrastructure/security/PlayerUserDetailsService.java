package org.codequistify.master.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.core.domain.player.port.PlayerReader;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerUserDetailsService implements UserDetailsService {

    private final PlayerReader playerReader;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        return new PlayerUserDetails(
                playerReader.findOneByUid(PolId.of(uid))
        );
    }
}