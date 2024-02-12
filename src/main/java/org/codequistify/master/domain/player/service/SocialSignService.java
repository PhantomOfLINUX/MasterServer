package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.player.dto.sign.LogInResponse;

public interface SocialSignService {
    String getSocialLogInURL();

    LogInResponse socialLogIn(String code);
}
