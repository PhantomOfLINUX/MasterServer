package org.codequistify.master.domain.player.service;

import org.codequistify.master.domain.authentication.dto.LogInRequest;
import org.codequistify.master.domain.authentication.dto.SignUpRequest;
import org.codequistify.master.domain.authentication.service.AuthenticationService;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class AuthenticationServiceTest {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationServiceTest(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @BeforeEach
    void setUp() {
        SignUpRequest request = new SignUpRequest("name", "email@pol.or.kr", "password");
        PlayerProfile result = authenticationService.signUp(request);
    }


    @Test
    @Order(1)
    public void 회원가입_성공(){
        SignUpRequest request = new SignUpRequest("new", "new@pol.or.kr", "password");
        PlayerProfile result = authenticationService.signUp(request);

        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.email(), result.email());
    }

    @Test
    public void 중복_회원가입(){
        SignUpRequest request = new SignUpRequest("name", "email@pol.or.kr", "password");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.signUp(request);
        });
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), ex.getMessage());
    }

    // 회원가입 이메일 양식

    // 회원가입 비밀번호 조건

    //로그인 성공
    @Test
    public void 정상적인_로그인() {
        LogInRequest request = new LogInRequest("email@pol.or.kr", "password");

        PlayerProfile result = authenticationService.logIn(request);

        assertNotNull(result);
        assertEquals(request.email(), result.email());
    }

    //로그인 비밀번호 오류
    @Test
    public void 로그인_비밀번호_오류() {
        LogInRequest request = new LogInRequest("email@pol.or.kr", "wrong");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.logIn(request);
        });
        assertEquals(ErrorCode.INVALID_EMAIL_OR_PASSWORD.getMessage(), ex.getMessage());
    }

    //로그인 이메일 오류
    @Test
    public void 로그인_이메일_오류() {
        LogInRequest request = new LogInRequest("email123@pol.or.kr", "password");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.logIn(request);
        });
        assertEquals(ErrorCode.INVALID_EMAIL_OR_PASSWORD.getMessage(), ex.getMessage());
    }



}