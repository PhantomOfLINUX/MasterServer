package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.player.dto.PlayerDTO;

public interface SocialSignService {
    String getSocialSignInURL();

    PlayerDTO socialLogin(String code);
}
