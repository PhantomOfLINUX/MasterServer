package org.codequistify.master.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerUserDetailsService implements UserDetailsService {

    private final PlayerQueryService playerQueryService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {
        return new PlayerUserDetails(
                playerQueryService.findOneByUid(uid)
        );
    }
}