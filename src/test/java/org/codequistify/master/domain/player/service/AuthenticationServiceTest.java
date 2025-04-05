package org.codequistify.master.domain.player.service;

import org.codequistify.master.application.authentication.dto.LogInRequest;
import org.codequistify.master.application.authentication.dto.SignUpRequest;
import org.codequistify.master.application.authentication.service.AuthenticationService;
import org.codequistify.master.application.player.dto.PlayerProfile;
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
        SignUpRequest request = new SignUpRequest("A", "A@pol.or.kr", "password99763892*");
        PlayerProfile result = authenticationService.signUp(request);
    }


    @Test
    @Order(1)
    public void 회원가입_성공(){
        SignUpRequest request = new SignUpRequest("B", "B@pol.or.kr", "password99763892*");
        PlayerProfile result = authenticationService.signUp(request);

        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.email(), result.email());
    }

    @Test
    public void 중복_회원가입(){
        SignUpRequest request = new SignUpRequest("A", "A@pol.or.kr", "password99763892*");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.signUp(request);
        });
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), ex.getMessage());
    }

    /*@Test
    public void 이메일_양식_미충족(){
        SignUpRequest request = new SignUpRequest("C", "", "password99763892*");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.signUp(request);
        });
        assertEquals(ErrorCode.INVALID_EMAIL_FORMAT.getMessage(), ex.getMessage());
    }*/

    @Test
    public void 비밀번호_조건_미충족(){
        SignUpRequest request = new SignUpRequest("D", "D@pol.or.kr", "pass63892");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.signUp(request);
        });
        assertEquals(ErrorCode.PASSWORD_POLICY_VIOLATION.getMessage(), ex.getMessage());
    }

    @Test
    public void 정상_로그인() {
        LogInRequest request = new LogInRequest("A@pol.or.kr", "password99763892*");

        PlayerProfile result = authenticationService.logIn(request);

        assertNotNull(result);
        assertEquals(request.email(), result.email());
    }

    //로그인 비밀번호 오류
    @Test
    public void 로그인_비밀번호_오류() {
        LogInRequest request = new LogInRequest("A@pol.or.kr", "wrong");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.logIn(request);
        });
        assertEquals(ErrorCode.INVALID_EMAIL_OR_PASSWORD.getMessage(), ex.getMessage());
    }

    //로그인 이메일 오류
    @Test
    public void 로그인_이메일_오류() {
        LogInRequest request = new LogInRequest("wrong@pol.or.kr", "password99763892*");

        Throwable ex = assertThrows(BusinessException.class, () -> {
            authenticationService.logIn(request);
        });
        assertEquals(ErrorCode.INVALID_EMAIL_OR_PASSWORD.getMessage(), ex.getMessage());
    }



}