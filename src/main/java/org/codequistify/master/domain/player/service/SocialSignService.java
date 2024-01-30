package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.player.dto.sign.SignInResponse;

public interface SocialSignService {
    String getSocialSignInURL();

    SignInResponse socialLogin(String code);
}
