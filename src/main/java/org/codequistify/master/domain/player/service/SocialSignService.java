package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.dto.SignInResponse;

public interface SocialSignService {
    String getSocialSignInURL();

    SignInResponse socialLogin(String code);
}
