package org.codequistify.master.domain.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.dto.LogInRequest;
import org.codequistify.master.domain.authentication.dto.LoginResponse;
import org.codequistify.master.domain.authentication.dto.SignUpRequest;
import org.codequistify.master.domain.authentication.dto.SocialLogInRequest;
import org.codequistify.master.domain.authentication.service.AuthenticationService;
import org.codequistify.master.domain.authentication.service.EmailVerificationService;
import org.codequistify.master.domain.authentication.service.impl.GoogleSocialSignService;
import org.codequistify.master.domain.authentication.service.impl.KakaoSocialSignService;
import org.codequistify.master.domain.authentication.service.impl.NaverSocialSignService;
import org.codequistify.master.domain.player.domain.Player;
import org.codequistify.master.domain.player.dto.PlayerProfile;
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

    private final EmailVerificationService emailVerificationService;
    private final TokenProvider tokenProvider;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    @Operation(
            summary = "구글 로그인 url 발급",
            description = "구글 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @LogMonitoring
    @GetMapping("auth/google/url")
    public String loginUrlGoogle() {
        return googleSocialSignService.getSocialLogInURL();
    }

    @Operation(
            summary = "카카오 로그인 url 발급",
            description = "카카오 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @LogMonitoring
    @GetMapping("auth/kakao/url")
    public String loginUrlKakao() {
        return kakaoSocialSignService.getSocialLogInURL();
    }

    @Operation(
            summary = "네이버 로그인 url 발급",
            description = "네이버 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @LogMonitoring
    @GetMapping("auth/naver/url")
    public String loginUrlNaver() {
        return naverSocialSignService.getSocialLogInURL();
    }


    private LoginResponse getLoginResponseWithToken(PlayerProfile playerProfile) {
        String refreshToken = tokenProvider.generateRefreshToken(playerProfile);
        authenticationService.updateRefreshToken(playerProfile.uid(), refreshToken); // refresh token db에 저장

        String accessToken = tokenProvider.generateAccessToken(playerProfile);

        TokenResponse tokenResponse = new TokenResponse(refreshToken, accessToken);
        return new LoginResponse(playerProfile, tokenResponse);
    }

    @Operation(
            summary = "구글 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 profile과 인증 토큰을 발급받는다."
    )
    @LogMonitoring
    @PostMapping("auth/google")
    public ResponseEntity<LoginResponse> socialSignInGoogle(@RequestBody SocialLogInRequest request, HttpServletResponse response) {
        PlayerProfile playerProfile = googleSocialSignService.socialLogIn(request.code());

        LoginResponse loginResponse = getLoginResponseWithToken(playerProfile);

        LOGGER.info("[socialSignInGoogle] 구글 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "카카오 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 profile과 인증 토큰을 발급받는다."
    )
    @LogMonitoring
    @PostMapping("auth/kakao")
    public ResponseEntity<LoginResponse> socialLogInKakao(@RequestBody SocialLogInRequest request) {
        PlayerProfile playerProfile = kakaoSocialSignService.socialLogIn(request.code());

        LoginResponse loginResponse = getLoginResponseWithToken(playerProfile);

        LOGGER.info("[socialSignInKakao] 카카오 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "네이버 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 profile과 인증 토큰을 발급받는다."
    )
    @LogMonitoring
    @PostMapping("auth/naver")
    public ResponseEntity<LoginResponse> socialLogInNaver(@RequestBody SocialLogInRequest request) {
        PlayerProfile playerProfile = kakaoSocialSignService.socialLogIn(request.code());

        LoginResponse loginResponse = getLoginResponseWithToken(playerProfile);

        LOGGER.info("[socialSignInNaver] 네이버 로그인, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "POL 자체 회원가입",
            description = "자체 회원가입이다. name, email, password를 필수로 입력받는다."
    )
    @LogMonitoring
    @PostMapping("/auth/signup")
    public ResponseEntity<LoginResponse> signUpPOL(@Valid @RequestBody SignUpRequest request) {
        PlayerProfile playerProfile = authenticationService.signUp(request);

        LoginResponse loginResponse = getLoginResponseWithToken(playerProfile);

        LOGGER.info("[SignUpPOL] pol 회원가입, Player: {}", playerProfile.uid());
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @Operation(
            summary = "POL 자체 로그인",
            description = "자체 로그인기능이다. name, password를 필수로 입력받는다."
    )
    @LogMonitoring
    @PostMapping("auth/login")
    public ResponseEntity<LoginResponse> logInPOL(@Valid @RequestBody LogInRequest request) {
        PlayerProfile playerProfile = authenticationService.logIn(request);

        LoginResponse loginResponse = getLoginResponseWithToken(playerProfile);

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
    @LogMonitoring
    @PostMapping("auth/refresh")
    public ResponseEntity<TokenResponse> regenerateAccessToken(TokenRequest request) {
        TokenResponse tokenResponse = authenticationService.regenerateAccessToken(request);

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
            summary = "회원가입 인증메일 발송",
            description = "회원가입 인증메일을 발송하는 요청이다. 중복된 이메일인지 함께 검증한다."
    )
    @LogMonitoring
    @GetMapping("auth/email/{email}/verification")
    public ResponseEntity<BasicResponse> sendAuthMail(@Valid @Email(message = "4102") @PathVariable String email) throws MessagingException {
        if (authenticationService.checkEmailDuplication(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        emailVerificationService.sendVerifyMail(email); // 비동기 요청 : 메일 전송에 시간이 너무 오래걸림

        LOGGER.info("[sendAuthMail] {} 인증 메일 전송", email);
        BasicResponse response = new BasicResponse(ErrorCode.SUCCESS.getCode(), "", "");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
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
