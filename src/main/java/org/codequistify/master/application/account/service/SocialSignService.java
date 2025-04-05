package org.codequistify.master.application.account.service;

import org.codequistify.master.application.account.vo.OAuthData;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.application.player.dto.PlayerProfile;

public interface SocialSignService {
    String getSocialLogInURL();
    PlayerProfile socialLogIn(OAuthData oAuthData);
    Player socialSignUp(OAuthData oAuthData);
}
