package org.codequistify.master.domain.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.domain.EmailVerification;
import org.codequistify.master.domain.authentication.domain.EmailVerificationType;
import org.codequistify.master.domain.authentication.dto.*;
import org.codequistify.master.domain.authentication.service.AuthenticationService;
import org.codequistify.master.domain.authentication.service.EmailVerificationService;
import org.codequistify.master.domain.authentication.service.impl.GithubSocialSignService;
import org.codequistify.master.domain.authentication.service.impl.GoogleSocialSignService;
import org.codequistify.master.domain.authentication.service.impl.KakaoSocialSignService;
import org.codequistify.master.domain.authentication.service.impl.NaverSocialSignService;
import org.codequistify.master.domain.authentication.vo.OAuthData;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.domain.player.service.PlayerDetailsService;
import org.codequistify.master.global.aspect.LogExecutionTime;
import org.codequistify.master.global.aspect.LogMethodInvocation;
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

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
@RequestMapping("api")
public class AuthenticationController {
    private final GoogleSocialSignService googleSocialSignService;
    private final KakaoSocialSignService kakaoSocialSignService;
    private final NaverSocialSignService naverSocialSignService;
    private final GithubSocialSignService githubSocialSignService;

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;
    private final PlayerDetailsService playerDetailsService;

    private final TokenProvider tokenProvider;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Operation(
            summary = "구글 로그인 url 발급",
            description = "구글 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @LogMonitoring
    @LogMethodInvocation
    @LogExecutionTime
    @GetMapping("auth/google/url")
    public ResponseEntity<BasicResponse> loginUrlGoogle() {
        BasicResponse response = BasicResponse.of(googleSocialSignService.getSocialLogInURL());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "카카오 로그인 url 발급",
            description = "카카오 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @LogMonitoring
    @GetMapping("auth/kakao/url")
    public ResponseEntity<BasicResponse> loginUrlKakao() {
        BasicResponse response = BasicResponse.of(kakaoSocialSignService.getSocialLogInURL());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "네이버 로그인 url 발급",
            description = "네이버 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @LogMonitoring
    @GetMapping("auth/naver/url")
    public ResponseEntity<BasicResponse> loginUrlNaver() {
        BasicResponse response = BasicResponse.of(naverSocialSignService.getSocialLogInURL());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @Operation(
            summary = "깃허브 로그인 url 발급",
            description = "깃허브 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @LogMonitoring
    @GetMapping("auth/github/url")
    public ResponseEntity<BasicResponse> loginUrlGithub() {
        BasicResponse response = BasicResponse.of(githubSocialSignService.getSocialLogInURL());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "구글 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 profile과 인증 토큰을 발급받는다."
    )
    @LogMonitoring
    @PostMapping("auth/google")
    public ResponseEntity<LoginResponse> socialSignInGoogle(@RequestBody SocialLogInRequest request, HttpServletResponse response) {
        OAuthData oAuthData = googleSocialSignService.getOAuthData(request.code());

        PlayerProfile playerProfile;
        try {
            playerProfile = googleSocialSignService.socialLogIn(oAuthData);
        } catch (BusinessException exception) {
            googleSocialSignService.socialSignUp(oAuthData); // 로그인 되어 있지 않을 때 회원가입 먼저 실행
            playerProfile = googleSocialSignService.socialLogIn(oAuthData);
        }

        TokenResponse tokenResponse = generateTokens(playerProfile);
        this.addTokenToCookie(tokenResponse, response);

        LoginResponse loginResponse = new LoginResponse(playerProfile, tokenResponse);

        LOGGER.info("[socialSignInGoogle] 구글 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "카카오 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 profile과 인증 토큰을 발급받는다."
    )
    @LogMonitoring
    @PostMapping("auth/kakao")
    public ResponseEntity<LoginResponse> socialLogInKakao(@RequestBody SocialLogInRequest request, HttpServletResponse response) {
        OAuthData oAuthData = kakaoSocialSignService.getOAuthData(request.code());

        PlayerProfile playerProfile;
        try {
            playerProfile = kakaoSocialSignService.socialLogIn(oAuthData);
        } catch (BusinessException exception) {
            kakaoSocialSignService.socialSignUp(oAuthData); // 로그인 되어 있지 않을 때 회원가입 먼저 실행
            playerProfile = kakaoSocialSignService.socialLogIn(oAuthData);
        }

        TokenResponse tokenResponse = generateTokens(playerProfile);
        this.addTokenToCookie(tokenResponse, response);

        LoginResponse loginResponse = new LoginResponse(playerProfile, tokenResponse);

        LOGGER.info("[socialSignInKakao] 카카오 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "네이버 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 profile과 인증 토큰을 발급받는다."
    )
    @LogMonitoring
    @PostMapping("auth/naver")
    public ResponseEntity<LoginResponse> socialLogInNaver(@RequestBody SocialLogInRequest request, HttpServletResponse response) {
        OAuthData oAuthData = naverSocialSignService.getOAuthData(request.code());

        PlayerProfile playerProfile;
        try {
            playerProfile = naverSocialSignService.socialLogIn(oAuthData);
        } catch (BusinessException exception) {
            naverSocialSignService.socialSignUp(oAuthData); // 로그인 되어 있지 않을 때 회원가입 먼저 실행
            playerProfile = naverSocialSignService.socialLogIn(oAuthData);
        }

        TokenResponse tokenResponse = generateTokens(playerProfile);
        this.addTokenToCookie(tokenResponse, response);

        LoginResponse loginResponse = new LoginResponse(playerProfile, tokenResponse);

        LOGGER.info("[socialSignInNaver] 네이버 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "깃허브 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 profile과 인증 토큰을 발급받는다."
    )
    @LogMonitoring
    @PostMapping("auth/github")
    public ResponseEntity<LoginResponse> socialLogInGithub(@RequestBody SocialLogInRequest request, HttpServletResponse response) {
        OAuthData oAuthData = githubSocialSignService.getOAuthData(request.code());

        PlayerProfile playerProfile;
        try {
            playerProfile = githubSocialSignService.socialLogIn(oAuthData);
        } catch (BusinessException exception) {
            githubSocialSignService.socialSignUp(oAuthData); // 로그인 되어 있지 않을 때 회원가입 먼저 실행
            playerProfile = githubSocialSignService.socialLogIn(oAuthData);
        }

        TokenResponse tokenResponse = generateTokens(playerProfile);
        this.addTokenToCookie(tokenResponse, response);

        LoginResponse loginResponse = new LoginResponse(playerProfile, tokenResponse);

        LOGGER.info("[socialSignInNaver] 깃허브 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "POL 자체 회원가입",
            description = "자체 회원가입이다. name, email, password를 필수로 입력받는다."
    )
    @LogMonitoring
    @PostMapping("/auth/signup")
    public ResponseEntity<LoginResponse> signUpPOL(@Valid @RequestBody SignUpRequest request, HttpServletResponse response) {
        // 이메일 인증 정보를 확인
        EmailVerification emailVerification = emailVerificationService
                .verifyEmailAndRetrieve(request.email(), EmailVerificationType.REGISTRATION);

        PlayerProfile playerProfile = authenticationService.signUp(request);

        // 인증메일 사용처리
        emailVerificationService.markEmailVerificationAsUsed(emailVerification);

        TokenResponse tokenResponse = generateTokens(playerProfile);
        this.addTokenToCookie(tokenResponse, response);

        LoginResponse loginResponse = new LoginResponse(playerProfile, tokenResponse);

        LOGGER.info("[SignUpPOL] pol 회원가입, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "POL 자체 로그인",
            description = "자체 로그인기능이다. name, password를 필수로 입력받는다."
    )
    @LogMonitoring
    @PostMapping("auth/login")
    public ResponseEntity<LoginResponse> logInPOL(@Valid @RequestBody LogInRequest request, HttpServletResponse response) {
        PlayerProfile playerProfile = authenticationService.logIn(request);

        TokenResponse tokenResponse = generateTokens(playerProfile);
        this.addTokenToCookie(tokenResponse, response);

        LoginResponse loginResponse = new LoginResponse(playerProfile, tokenResponse);

        LOGGER.info("[LogInPOL] pol 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "엑세스 토큰 재발급",
            description = """
                    AccessToken을 재발급 한다.
                    RefreshToken 값을 body로 받는다. 이때 token은 'Bearer' 없이 token 값만을 적어야 한다.""",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 재발급"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청, id 불일치 또는 빈 요청 등"),
                    @ApiResponse(responseCode = "401", description = "만료된 Refresh Token")
            }
    )
    @LogMonitoring
    @PostMapping("auth/refresh")
    public ResponseEntity<TokenResponse> regenerateAccessToken(@RequestBody TokenRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authenticationService.regenerateAccessToken(request);
        this.addTokenToCookie(tokenResponse, response);

        LOGGER.info("[regenerateAccessToken] AccessToken 재발급");
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @Operation(
            summary = "토큰 해석",
            description = "토큰 정보를 해석한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "유효한 토큰"),
                    @ApiResponse(responseCode = "400", description = "잘못된 양식의 토큰 또는 셔명되지 않은 토큰 "),
            }
    )
    @LogMonitoring
    @GetMapping("auth/validate")
    public ResponseEntity<TokenInfo> analyzeToken(@RequestParam String token) {
        try {
            TokenInfo tokenInfo = authenticationService.analyzeTokenInfo(token);
            return ResponseEntity.status(HttpStatus.OK).body(tokenInfo);
        } catch (NullPointerException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
            summary = "로그아웃 요청",
            description = "Authorization에 토큰이 있어야 한다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "잘못된 토큰 정보")
            }
    )
    @LogMonitoring
    @PostMapping("auth/logout")
    public ResponseEntity<BasicResponse> LogOut(HttpServletRequest request, HttpServletResponse servletResponse) {
        String token = tokenProvider.resolveToken(request);
        String aud = tokenProvider.getAudience(token);
        Player player = playerDetailsService.findOnePlayerByUid(aud);
        if (player == null) {
            throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.UNAUTHORIZED);
        }
        authenticationService.logOut(player);
        LOGGER.info("[LogOut] Player: {}, 로그아웃 완료", player.getUid());

        this.removeTokenFromCookie(servletResponse);
        this.removeTokenFromCookie_DEV(servletResponse);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BasicResponse.of("SUCCESS"));
    }

    @Operation(
            summary = "이메일 중복 검사",
            description = "이메일 중복 검사이다. 등록되어 있는지 여부를 확인한다. 사용할 수 없으면 error에 내용이 담긴다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용가능한 이메일",
                            content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class))),
                    @ApiResponse(responseCode = "400", description = "사용할 수 없는 이메일",
                            content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class)))}
    )
    @LogMonitoring
    @GetMapping("auth/email/{email}")
    public ResponseEntity<BasicResponse> checkEmailDuplication(@PathVariable String email) {
        if (authenticationService.checkEmailDuplication(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BasicResponse.of("사용가능한 이메일입니다."));
        }
    }

    @Operation(
            summary = "인증메일 발송 요청",
            description = """
                    회원가입 인증메일을 발송하는 요청이다. 중복된 이메일인지 함께 검증한다.
                    type 목록은 다음과 같다.
                    - 회원가입 : *'REGISTRATION'*
                    - 비밀번호 초기화 : *'PASSWORD_RESET'*"""
    )
    @PostMapping("auth/email/verification")
    @LogMonitoring
    public ResponseEntity<BasicResponse> sendAuthMail(@Valid @RequestBody EmailVerificationRequest request) throws MessagingException {
        LOGGER.info(request.type().toString());
        if (request.type() == EmailVerificationType.REGISTRATION){
            if (authenticationService.checkEmailDuplication(request.email())) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
        }
        if (request.type() == EmailVerificationType.PASSWORD_RESET) {
            if (!authenticationService.checkEmailDuplication(request.email())) {
                throw new BusinessException(ErrorCode.PLAYER_NOT_FOUND, HttpStatus.NOT_FOUND);
            }
        }

        emailVerificationService.sendVerifyMail(request.email(), request.type()); // 비동기 요청 : 메일 전송에 시간이 너무 오래걸림

        LOGGER.info("[sendAuthMail] {} 인증 메일 전송", request.email());
        BasicResponse response = new BasicResponse(ErrorCode.SUCCESS.getCode(), "", "");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(
            hidden = true,
            summary = "회원가입 인증코드 확인",
            description = "회원가입 인증코드를 확인한다. 사용자의 email과 입력 코드를 path로 받는다. 결과는 response 필드에 \"true\", \"false\"로 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 결과", content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class)))}
    )
    @LogMonitoring
    @GetMapping("auth/email/{email}/code/{code}")
    public ResponseEntity<BasicResponse> verifyCode(@PathVariable String email, @PathVariable String code) {
        code = code.trim();
        String bool = Boolean.toString(emailVerificationService.verifyCode(email, code));

        LOGGER.info("[verifyCode] {} 회원가입 메일 코드 인증 {}", email, bool);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BasicResponse.of(bool));
    }

    @Operation(
            summary = "임시 엑세스 토큰 발급",
            description = "비밀번호 찾기를 위한 임시 엑세스 토큰을 발급한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 성공",
                            content = @Content(
                                    schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "400", description = "인증 실패",
                            content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class)))}
    )
    @LogMonitoring
    @GetMapping("/auth/access/{email}/temp")
    public ResponseEntity<TokenResponse> generateTempToken(@PathVariable String email) {
        EmailVerification emailVerification = emailVerificationService // 인증정보 없으면 서비스에서 예외
                .getEmailVerificationByEmail(email, false, EmailVerificationType.PASSWORD_RESET);

        emailVerificationService.markEmailVerificationAsUsed(emailVerification);

        String accessToken = tokenProvider.generateTempToken(email);
        TokenResponse response = new TokenResponse("", accessToken);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    private TokenResponse generateTokens(PlayerProfile playerProfile) {
        String refreshToken = tokenProvider.generateRefreshToken(playerProfile);
        authenticationService.updateRefreshToken(playerProfile.uid(), refreshToken); // refresh token db에 저장

        String accessToken = tokenProvider.generateAccessToken(playerProfile);

        return new TokenResponse(refreshToken, accessToken);
    }

    private void addTokenToCookie(TokenResponse tokenResponse, HttpServletResponse response) {
        addRefreshTokenToCookie(tokenResponse.refreshToken(), response);
        addAccessTokenToCookie(tokenResponse.accessToken(), response);
        addRefreshTokenToCookie_DEV(tokenResponse.refreshToken(), response);
        addAccessTokenToCookies_DEV(tokenResponse.accessToken(), response);
    }
    private void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("POL_REFRESH_TOKEN", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setDomain("pol.or.kr");
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 일주일

        response.addCookie(refreshTokenCookie);
    }
    private void addAccessTokenToCookie(String accessToken, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("POL_ACCESS_TOKEN", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setDomain("pol.or.kr");
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60); // 한 시간

        response.addCookie(accessTokenCookie);
    }

    private void removeTokenFromCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("POL_REFRESH_TOKEN", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setDomain("pol.or.kr");
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 지속시간을 0으로 하여 덮어쓰기

        Cookie accessTokenCookie = new Cookie("POL_ACCESS_TOKEN", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setDomain("pol.or.kr");
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        response.addCookie(refreshTokenCookie);
        response.addCookie(accessTokenCookie);
    }

    private void removeTokenFromCookie_DEV(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("POL_REFRESH_TOKEN_DEV", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setDomain("localhost");
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);

        Cookie accessTokenCookie = new Cookie("POL_ACCESS_TOKEN_DEV", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setDomain("localhost");
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);

        response.addCookie(refreshTokenCookie);
        response.addCookie(accessTokenCookie);
    }

    //TODO 임시
    private void addRefreshTokenToCookie_DEV(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("POL_REFRESH_TOKEN_DEV", refreshToken);
        refreshTokenCookie.setDomain("localhost");
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(refreshTokenCookie);
    }

    //TODO 임시
    private void addAccessTokenToCookies_DEV(String accessToken, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("POL_ACCESS_TOKEN_DEV", accessToken);
        accessTokenCookie.setDomain("localhost");
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60);

        response.addCookie(accessTokenCookie);
    }


















    /*
    //서버용인증
    @GetMapping("auth/google")
    @Operation(hidden = true)
    public PlayerProfile googleLogin(@RequestParam String code) {
        return googleSocialSignService.socialLogIn(code);
    }

    //서버용인증
    @Operation(hidden = true)
    @GetMapping("auth/kakao")
    public PlayerProfile kakaoLogin(@RequestParam String code) {
        return kakaoSocialSignService.socialLogIn(code);
    }

    //서버용인증
    @GetMapping("auth/callback/naver")
    @Operation()
    public ResponseEntity<?> naverLogin(@RequestParam String code, HttpServletResponse response) {
        SocialLogInRequest request = new SocialLogInRequest(code);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(socialLogInNaver(request, response));
    }

    //서버용인증
    @GetMapping("auth/callback/github")
    @Operation()
    public ResponseEntity<?> githubLogin(@RequestParam String code, HttpServletResponse response) {
        SocialLogInRequest request = new SocialLogInRequest(code);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(socialLogInGithub(request, response));
    }

     */

}
