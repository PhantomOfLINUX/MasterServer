package org.codequistify.master.domain.authentication.service;

import org.codequistify.master.domain.authentication.dto.LogInResponse;
import org.codequistify.master.domain.authentication.vo.OAuthResourceVO;
import org.codequistify.master.domain.player.domain.Player;

public interface SocialSignService {
    String getSocialLogInURL();
    LogInResponse socialLogIn(String code);
    Player socialSignUp(OAuthResourceVO resource);
}
