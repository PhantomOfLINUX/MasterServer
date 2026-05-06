package org.codequistify.master.domain.authentication.service;

import org.codequistify.master.domain.authentication.vo.OAuthData;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;

public interface SocialSignService {
    String getSocialLogInURL();
    PlayerProfile socialLogIn(OAuthData oAuthData);
    Player socialSignUp(OAuthData oAuthData);
}
