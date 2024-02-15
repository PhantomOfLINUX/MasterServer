package org.codequistify.master.domain.authentication.service;

import org.codequistify.master.domain.authentication.vo.OAuthResourceVO;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;

public interface SocialSignService {
    String getSocialLogInURL();
    PlayerProfile socialLogIn(String code);
    Player socialSignUp(OAuthResourceVO resource);
}
