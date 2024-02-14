package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.player.domain.OAuthType;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.sign.LogInResponse;
import org.codequistify.master.domain.player.dto.sign.SignUpRequest;
import org.codequistify.master.domain.player.repository.PlayerRepository;
import org.codequistify.master.global.exception.common.BusinessException;
import org.codequistify.master.global.exception.common.ErrorCode;
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
        SignUpRequest request = new SignUpRequest("name", "email", "password");
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

        SignUpRequest request = new SignUpRequest("name", "email", "password");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            signService.signUp(request);
        });
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), ex.getMessage());
    }

    // 회원가입 이메일 양식

    // 회원가입 비밀번호 조건

    //로그인 성공

    //로그인 비밀번호 오류

    //로그인 패스워드 오류

    //이메일 중복 검사 성공

    //이메일 중복 검사 실패


}