package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.sign.LogInResponse;
import org.codequistify.master.domain.player.vo.OAuthResourceVO;

public interface SocialSignService {
    String getSocialLogInURL();
    LogInResponse socialLogIn(String code);
    Player socialSignUp(OAuthResourceVO resource);
}
