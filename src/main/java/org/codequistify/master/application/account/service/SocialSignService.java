package org.codequistify.master.application.account.service;

import org.codequistify.master.application.account.vo.OAuthProfile;
import org.codequistify.master.core.domain.player.model.Player;

public interface SocialSignService {
    String getSocialLogInURL();

    Player socialLogIn(OAuthProfile oAuthProfile);

    Player socialSignUp(OAuthProfile oAuthProfile);

    OAuthProfile getOAuthProfile(String code);
}
