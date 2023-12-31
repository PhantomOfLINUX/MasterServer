package org.codequistify.master.domain.player.service.impl;

import jakarta.persistence.EntityExistsException;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.domain.repository.PlayerRepository;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.dto.SignRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

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
        PlayerDTO result = signService.signUp(request);

        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.email(), result.email());
    }

    @Test
    public void 중복_회원가입_실패(){
        Player player = new Player("name", "email", "password", "pol", 0L, 0);
        playerRepository.save(player);

        SignRequest request = new SignRequest(null, "name", "email", "password");

        assertThrows(EntityExistsException.class, () -> {
            signService.signUp(request);
        });
    }


}