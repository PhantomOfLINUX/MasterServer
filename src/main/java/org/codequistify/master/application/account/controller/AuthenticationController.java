package org.codequistify.master.application.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.application.account.dto.*;
import org.codequistify.master.application.account.service.AccountService;
import org.codequistify.master.application.account.service.EmailVerificationService;
import org.codequistify.master.application.account.service.SocialSignService;
import org.codequistify.master.application.account.support.SocialSignHandlerMap;
import org.codequistify.master.application.account.support.TokenCookieProvider;
import org.codequistify.master.application.account.support.TokenGenerator;
import org.codequistify.master.application.account.vo.OAuthProfile;
import org.codequistify.master.application.exception.ApplicationException;
import org.codequistify.master.application.player.service.PlayerQueryService;
import org.codequistify.master.core.domain.account.model.EmailVerification;
import org.codequistify.master.core.domain.account.model.EmailVerificationType;
import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.codequistify.master.global.exception.ErrorCode;
import org.codequistify.master.global.exception.domain.BusinessException;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.jwt.dto.TokenInfo;
import org.codequistify.master.global.jwt.dto.TokenRequest;
import org.codequistify.master.global.jwt.dto.TokenResponse;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
@RequestMapping("api")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final SocialSignHandlerMap handlerMap;
    private final AccountService accountService;
    private final EmailVerificationService emailVerificationService;
    private final PlayerQueryService playerQueryService;

    private final TokenProvider tokenProvider;
    private final TokenCookieProvider tokenCookieProvider;
    private final TokenGenerator tokenGenerator;

    @GetMapping("/{provider}/url")
    @Operation(summary = "소셜 로그인 URL 발급", description = "구글/카카오/네이버/깃허브 로그인 URL을 반환")
    public ResponseEntity<BasicResponse> getLoginUrl(
            @Parameter(
                    name = "provider",
                    description = "로그인 플랫폼 (google, kakao, naver, github 중 하나)",
                    in = ParameterIn.PATH,
                    required = true,
                    schema = @Schema(type = "string", allowableValues = {"google", "kakao", "naver", "github"})
            )
            @PathVariable String provider
    ) {
        return ResponseEntity.ok(
                BasicResponse.of(handlerMap.getHandler(provider).getSocialLogInURL())
        );
    }

    @PostMapping("/{provider}")
    @Operation(summary = "소셜 로그인 요청", description = "소셜 code로 로그인 처리")
    public ResponseEntity<LoginResponse> login(@Parameter(
                                                       name = "provider",
                                                       description = "로그인 플랫폼 (google, kakao, naver, github 중 하나)",
                                                       in = ParameterIn.PATH,
                                                       required = true,
                                                       schema = @Schema(type = "string", allowableValues = {"google", "kakao", "naver", "github"})
                                               )
                                               @PathVariable String provider,
                                               @RequestBody SocialLogInRequest request,
                                               HttpServletResponse response) {
        SocialSignService handler = handlerMap.getHandler(provider);
        OAuthProfile profile = handler.getOAuthProfile(request.code());

        Player player = trySocialLogin(
                () -> handler.socialLogIn(profile),
                () -> handler.socialSignUp(profile),
                () -> handler.socialLogIn(profile)
        );

        TokenResponse token = tokenGenerator.generate(player);
        tokenCookieProvider.addTokenCookies(response, token);
        return ResponseEntity.ok(new LoginResponse(player, token));
    }

    private Player trySocialLogin(Supplier<Player> login, Runnable signUp, Supplier<Player> reLogin) {
        try {
            return login.get();
        } catch (BusinessException ex) {
            signUp.run();
            return reLogin.get();
        }
    }

    /**
     * POL 자체 회원가입
     */
    @Operation(summary = "POL 자체 회원가입", description = "자체 회원가입 (메일인증 필수)")
    @LogMonitoring
    @PostMapping("/auth/signup")
    public ResponseEntity<LoginResponse> signUpPOL(@Valid @RequestBody SignUpRequest request,
                                                   HttpServletResponse response) {
        EmailVerification emailVerification = emailVerificationService.verifyEmailAndRetrieve(request.email(),
                                                                                              EmailVerificationType.REGISTRATION);
        Player player = accountService.signUp(request.name(), request.email(), request.password());
        emailVerificationService.markEmailVerificationAsUsed(emailVerification);

        TokenResponse token = tokenGenerator.generate(player);
        tokenCookieProvider.addTokenCookies(response, token);
        return ResponseEntity.ok(new LoginResponse(player, token));
    }

    /**
     * POL 자체 로그인
     */
    @Operation(summary = "POL 자체 로그인", description = "자체 로그인 (name, password 필요)")
    @LogMonitoring
    @PostMapping("auth/login")
    public ResponseEntity<LoginResponse> logInPOL(@Valid @RequestBody LogInRequest request,
                                                  HttpServletResponse response) {
        Player player = accountService.logIn(request.email(), request.password());
        TokenResponse token = tokenGenerator.generate(player);
        tokenCookieProvider.addTokenCookies(response, token);
        return ResponseEntity.ok(new LoginResponse(player, token));
    }

    /**
     * 엑세스 토큰 재발급
     */
    @Operation(summary = "엑세스 토큰 재발급", description = "RefreshToken 으로 AccessToken 재발급")
    @LogMonitoring
    @PostMapping("auth/refresh")
    public ResponseEntity<TokenResponse> regenerateAccessToken(@RequestBody TokenRequest request,
                                                               HttpServletResponse response) {
        TokenResponse token = accountService.regenerateAccessToken(request);
        tokenCookieProvider.addTokenCookies(response, token);
        return ResponseEntity.ok(token);
    }

    /**
     * 토큰 해석
     */
    @Operation(summary = "토큰 해석", description = "토큰 정보 해석 및 유효성 체크")
    @LogMonitoring
    @GetMapping("auth/validate")
    public ResponseEntity<TokenInfo> analyzeToken(@RequestParam String token) {
        return Optional.of(token)
                       .map(accountService::analyzeTokenInfo)
                       .map(ResponseEntity::ok)
                       .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_TOKEN, HttpStatus.BAD_REQUEST));
    }

    /**
     * 로그아웃 요청
     */
    @Operation(summary = "로그아웃 요청", description = "Authorization 헤더의 토큰이 필요")
    @LogMonitoring
    @PostMapping("auth/logout")
    public ResponseEntity<BasicResponse> logOut(HttpServletRequest request, HttpServletResponse response) {
        String token = tokenProvider.resolveToken(request);
        String aud = tokenProvider.getAudience(token);

        Player player = playerQueryService.findOneByUid(PolId.of(aud));
        Optional.ofNullable(player)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.UNAUTHORIZED));

        accountService.logOut(player);

        tokenCookieProvider.removeTokenCookies(response);
        return ResponseEntity.ok(BasicResponse.of("SUCCESS"));
    }

    /**
     * 이메일 중복 검사
     */
    @Operation(summary = "이메일 중복 검사", description = "이메일이 사용가능한지 체크")
    @LogMonitoring
    @GetMapping("auth/email/{email}")
    public ResponseEntity<BasicResponse> checkEmailDuplication(@PathVariable String email) {
        boolean duplicated = accountService.checkEmailDuplication(email);
        if (duplicated) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(BasicResponse.of("사용가능한 이메일입니다."));
    }

    /**
     * 인증메일 발송 요청
     */
    @PostMapping("auth/email/verification")
    @Operation(summary = "인증메일 발송 요청")
    @LogMonitoring
    public ResponseEntity<BasicResponse> sendAuthMail(@Valid @RequestBody EmailVerificationRequest request) {
        emailVerificationService.validateAndSend(request.email(), request.type());
        return ResponseEntity.ok(BasicResponse.of("인증 메일 전송 완료"));
    }

    /**
     * 회원가입 인증코드 확인
     */
    @Operation(hidden = true)
    @LogMonitoring
    @GetMapping("auth/email/{email}/code/{code}")
    public ResponseEntity<BasicResponse> verifyCode(@PathVariable String email, @PathVariable String code) {
        boolean verified = emailVerificationService.verifyCode(email, code.trim());
        return ResponseEntity.ok(BasicResponse.of(String.valueOf(verified)));
    }

    /**
     * 임시 엑세스 토큰 발급
     */
    @Operation(summary = "임시 엑세스 토큰 발급", description = "비밀번호 찾기용 임시 AccessToken 발급")
    @LogMonitoring
    @GetMapping("/auth/access/{email}/temp")
    public ResponseEntity<TokenResponse> generateTempToken(@PathVariable String email) {
        EmailVerification verification = emailVerificationService.getEmailVerificationByEmail(
                email, false, EmailVerificationType.PASSWORD_RESET
        );
        emailVerificationService.markEmailVerificationAsUsed(verification);

        String tempToken = tokenProvider.generateTempToken(email);
        return ResponseEntity.ok(new TokenResponse("", tempToken));
    }


}
