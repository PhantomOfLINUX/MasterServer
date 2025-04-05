package org.codequistify.master.application.account.support;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.service.AccountService;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.jwt.dto.TokenResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenGenerator {

    private final AccountService accountService;
    private final TokenProvider tokenProvider;

    public TokenResponse generate(Player player) {
        String refreshToken = tokenProvider.generateRefreshToken(player);
        accountService.updateRefreshToken(player.getUid(), refreshToken);

        String accessToken = tokenProvider.generateAccessToken(player);

        return new TokenResponse(refreshToken, accessToken);
    }
}
