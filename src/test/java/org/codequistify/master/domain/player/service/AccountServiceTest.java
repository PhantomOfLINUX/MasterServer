package org.codequistify.master.domain.player.service;

import org.codequistify.master.application.account.service.AccountService;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.exception.ErrorCode;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.vo.Email;
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
class AccountServiceTest {
    private final AccountService accountService;

    @Autowired
    public AccountServiceTest(AccountService accountService) {
        this.accountService = accountService;
    }

    @BeforeEach
    void setUp() {
        accountService.signUp("A", Email.of("A@pol.or.kr"), "password99763892*");
    }

    @Test
    @Order(1)
    public void 회원가입_성공() {
        Player result = accountService.signUp("B", Email.of("B@pol.or.kr"), "password99763892*");

        assertNotNull(result);
        assertEquals("B", result.getName());
        assertEquals(Email.of("B@pol.or.kr"), result.getEmail());
    }

    @Test
    @Order(2)
    public void 중복_회원가입() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> {
            accountService.signUp("A", Email.of("A@pol.or.kr"), "password99763892*");
        });

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage(), ex.getMessage());
    }

    @Test
    @Order(3)
    public void 비밀번호_조건_미충족() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> {
            accountService.signUp("D", Email.of("D@pol.or.kr"), "pass63892");
        });

        assertEquals(ErrorCode.PASSWORD_POLICY_VIOLATION.getMessage(), ex.getMessage());
    }

    @Test
    @Order(4)
    public void 정상_로그인() {
        Player result = accountService.logIn(Email.of("A@pol.or.kr"), "password99763892*");

        assertNotNull(result);
        assertEquals(Email.of("A@pol.or.kr"), result.getEmail());
    }

    @Test
    @Order(5)
    public void 로그인_비밀번호_오류() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> {
            accountService.logIn(Email.of("A@pol.or.kr"), "wrong");
        });

        assertEquals(ErrorCode.INVALID_EMAIL_OR_PASSWORD.getMessage(), ex.getMessage());
    }

    @Test
    @Order(6)
    public void 로그인_이메일_오류() {
        ApplicationException ex = assertThrows(ApplicationException.class, () -> {
            accountService.logIn(Email.of("wrong@pol.or.kr"), "password99763892*");
        });

        assertEquals(ErrorCode.INVALID_EMAIL_OR_PASSWORD.getMessage(), ex.getMessage());
    }
}
