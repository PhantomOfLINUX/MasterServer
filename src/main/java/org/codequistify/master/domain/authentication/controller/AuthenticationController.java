package org.codequistify.master.domain.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.dto.LogInRequest;
import org.codequistify.master.domain.authentication.dto.LoginResponse;
import org.codequistify.master.domain.authentication.dto.SignUpRequest;
import org.codequistify.master.domain.authentication.dto.SocialLogInRequest;
import org.codequistify.master.domain.authentication.service.AuthenticationService;
import org.codequistify.master.domain.authentication.service.MailVerificationService;
import org.codequistify.master.domain.authentication.service.impl.GoogleSocialSignService;
import org.codequistify.master.domain.authentication.service.impl.KakaoSocialSignService;
import org.codequistify.master.domain.authentication.service.impl.NaverSocialSignService;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
import org.codequistify.master.global.exception.common.BusinessException;
import org.codequistify.master.global.exception.common.ErrorCode;
import org.codequistify.master.global.jwt.TokenProvider;
import org.codequistify.master.global.jwt.dto.TokenInfo;
import org.codequistify.master.global.jwt.dto.TokenRequest;
import org.codequistify.master.global.jwt.dto.TokenResponse;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication")
@RequestMapping("api")
public class AuthenticationController {
    private final GoogleSocialSignService googleSocialSignService;
    private final KakaoSocialSignService kakaoSocialSignService;
    private final NaverSocialSignService naverSocialSignService;
    private final AuthenticationService authenticationService;

    private final MailVerificationService mailVerificationService;
    private final TokenProvider tokenProvider;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Operation(
            summary = "구글 로그인 url 발급",
            description = "구글 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @GetMapping("auth/google/url")
    public String loginUrlGoogle() {
        return googleSocialSignService.getSocialLogInURL();
    }

    @Operation(
            summary = "카카오 로그인 url 발급",
            description = "카카오 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @GetMapping("auth/kakao/url")
    public String loginUrlKakao() {
        return kakaoSocialSignService.getSocialLogInURL();
    }

    @Operation(
            summary = "네이버 로그인 url 발급",
            description = "네이버 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @GetMapping("auth/naver/url")
    public String loginUrlNaver() {
        return naverSocialSignService.getSocialLogInURL();
    }

    @Operation(
            summary = "구글 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 'email'과 'name'을 반환받는다."
    )
    @PostMapping("auth/google")
    public ResponseEntity<PlayerProfile> socialSignInGoogle(@RequestBody SocialLogInRequest request, HttpServletResponse response) {
        PlayerProfile playerProfile = googleSocialSignService.socialLogIn(request.code());

        String refreshToken = tokenProvider.generateRefreshToken(playerProfile);
        String accessToken = tokenProvider.generateAccessToken(playerProfile);

        addAccessTokensToCookies(accessToken, response);
        addRefreshTokensToCookies(refreshToken, response);
        authenticationService.updateRefreshToken(playerProfile.uid(), refreshToken); // refresh token db에 저장

        LOGGER.info("[socialSignInGoogle] {} 구글 로그인", playerProfile.email());
        return new ResponseEntity<>(playerProfile, HttpStatus.OK);
    }

    @Operation(
            summary = "카카오 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 'email'과 'knickname'을 반환받는다."
    )
    @PostMapping("auth/kakao")
    public ResponseEntity<PlayerProfile> socialLogInKakao(@RequestBody SocialLogInRequest request) {
        PlayerProfile playerProfile = kakaoSocialSignService.socialLogIn(request.code());

        LOGGER.info("{} kakao 로그인", playerProfile.email());
        return new ResponseEntity<>(playerProfile, HttpStatus.OK);
    }

    @Operation(
            summary = "POL 자체 회원가입",
            description = "자체 회원가입이다. name, email, password를 필수로 입력받는다."
    )
    @PostMapping("/auth/signup")
    public ResponseEntity<LoginResponse> SignUpPOL(@Valid @RequestBody SignUpRequest request, HttpServletResponse response) {
        PlayerProfile playerProfile = authenticationService.signUp(request);

        String refreshToken = tokenProvider.generateRefreshToken(playerProfile);
        addRefreshTokensToCookies(refreshToken, response);
        authenticationService.updateRefreshToken(playerProfile.uid(), refreshToken); // refresh token db에 저장

        String accessToken = tokenProvider.generateAccessToken(playerProfile);

        LoginResponse loginResponse = new LoginResponse(playerProfile, accessToken);

        LOGGER.info("[SignUpPOL] pol 회원가입, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "POL 자체 로그인",
            description = "자체 로그인기능이다. name, password를 필수로 입력받는다."
    )
    @PostMapping("auth/login")
    public ResponseEntity<LoginResponse> LogInPOL(@Valid @RequestBody LogInRequest request, HttpServletResponse response) {
        PlayerProfile playerProfile = authenticationService.logIn(request);

        String refreshToken = tokenProvider.generateRefreshToken(playerProfile);
        addRefreshTokensToCookies(refreshToken, response);
        authenticationService.updateRefreshToken(playerProfile.uid(), refreshToken); // refresh token db에 저장

        String accessToken = tokenProvider.generateAccessToken(playerProfile);

        LoginResponse loginResponse = new LoginResponse(playerProfile, accessToken);

        LOGGER.info("[LogInPOL] pol 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "엑세스 토큰 재발급",
            description = "AccessToken을 재발급 한다.\n\n" +
                    " RefreshToken 값을 body로 받는다. 이때 token은 'Bearer' 없이 token 값만을 적어야 한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 재발급"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청, id 불일치 또는 빈 요청 등"),
                    @ApiResponse(responseCode = "401", description = "만료된 Refresh Token")
            }
    )
    @PostMapping("auth/refresh")
    public ResponseEntity<TokenResponse> regenerateAccessToken(TokenRequest request, HttpServletResponse response) {
        TokenResponse tokenResponse = authenticationService.regenerateRefreshToken(request);

        addRefreshTokensToCookies(tokenResponse.refreshToken(), response);

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
    @GetMapping("auth/validate")
    public ResponseEntity<TokenInfo> analyzeToken(@RequestParam String token) {
        try {
            TokenInfo tokenInfo = authenticationService.analyzeTokenInfo(token);
            return ResponseEntity.status(HttpStatus.OK).body(tokenInfo);
        } catch (NullPointerException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, HttpStatus.BAD_REQUEST);
        }
    }

    private void addAccessTokensToCookies(String accessToken, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("AccessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setDomain("localhost");
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60); // 한 시간

        response.addCookie(accessTokenCookie);
    }

    private void addRefreshTokensToCookies(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("RefreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setDomain("localhost");
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(24 * 60 * 60); // 하루

        response.addCookie(refreshTokenCookie);
    }






    @Operation(
            summary = "로그아웃 요청",
            description = "Authorization에 토큰이 있어야 한다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "잘못된 토큰 정보")
            }
    )
    @PostMapping("auth/logout")
    public ResponseEntity<BasicResponse> LogOut(@AuthenticationPrincipal Player player) {
        authenticationService.logOut(player);
        LOGGER.info("[LogOut] Player: {}, 로그아웃 완료", player.getUid());

        return ResponseEntity.status(HttpStatus.OK)
                .body(BasicResponse.of("SUCCESS"));
    }

    @Operation(
            summary = "이메일 중복 검사",
            description = "이메일 중복 검사이다. 등록되어 있는지 여부를 확인한다. 사용할 수 없으면 error에 내용이 담긴다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용가능한 이메일",
                            content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class),
                                    examples = @ExampleObject(value = "{\"response\":\"Non Exist This Email\"}"))),
                    @ApiResponse(responseCode = "400", description = "사용할 수 없는 이메일",
                            content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class),
                                    examples = @ExampleObject(value = "{\"error\":\"Already Exist This Email\"}")))}
    )
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
            summary = "회원가입 인증메일 발송",
            description = "회원가입 인증메일을 발송하는 요청이다. 응답값이 존재하지 않는 요청이다."
    )
    @GetMapping("auth/email/{email}/verify")
    public ResponseEntity<Void> sendAuthMail(@PathVariable String email) throws MessagingException {
        mailVerificationService.sendVerifyMail(email);

        LOGGER.info("[sendAuthMail] {} 인증 메일 전송", email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(null);
    }

    @Operation(
            summary = "회원가입 인증코드 확인",
            description = "회원가입 인증코드를 확인한다. 사용자의 email과 입력 코드를 path로 받는다. 결과는 response 필드에 \"true\", \"false\"로 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 결과", content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class),
                                    examples = {
                                            @ExampleObject(name = "올바른 코드", value = "{\"response\":\"true\"}"),
                                            @ExampleObject(name = "잘못된 코드", value = "{\"response\":\"false\"}")}))}
    )
    @GetMapping("auth/email/{email}/code/{code}")
    public ResponseEntity<BasicResponse> verifyCode(@PathVariable String email, @PathVariable String code) {
        code = code.trim();
        String bool = Boolean.toString(mailVerificationService.verifyCode(email, code));

        LOGGER.info("[verifyCode] {} 회원가입 메일 코드 인증 {}", email, bool);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BasicResponse.of(bool));
    }




















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
    @Operation(hidden = true)
    public PlayerProfile naverLogin(@RequestParam String code) {
        return naverSocialSignService.socialLogIn(code);
    }

}
