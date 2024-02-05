package org.codequistify.master.domain.player.service.impl;

import jakarta.persistence.EntityExistsException;
import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.sign.LogInResponse;
import org.codequistify.master.domain.player.dto.sign.SignRequest;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.domain.player.service.SignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
class SignServiceTest {
    private final PlayerRepository playerRepository;
    private final SignService signService;

    @Autowired
    public SignServiceTest(PlayerRepository playerRepository, SignService signService) {
        this.playerRepository = playerRepository;
        this.signService = signService;
    }

    @Test
    public void 회원가입_성공_POL(){
        SignRequest request = new SignRequest(null, "name", "email", "password");
        LogInResponse result = signService.signUp(request);

        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.email(), result.email());
    }

    @Test
    public void 중복_회원가입_실패(){
        Player player = Player.builder()
                .name("name")
                .email("email")
                .password("password")
                .oAuthType(OAuthType.POL)
                .oAuthId("")
                .level(0).build();
        playerRepository.save(player);

        SignRequest request = new SignRequest(null, "name", "email", "password");

        assertThrows(EntityExistsException.class, () -> {
            signService.signUp(request);
        });
    }


}