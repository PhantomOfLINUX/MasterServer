package org.codequistify.master.domain.player.controller;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.dto.sign.LogOutRequest;
import org.codequistify.master.domain.player.dto.sign.SignInResponse;
import org.codequistify.master.domain.player.dto.sign.SignRequest;
import org.codequistify.master.domain.player.service.impl.GoogleSocialSignService;
import org.codequistify.master.domain.player.service.impl.KakaoSocialSignService;
import org.codequistify.master.domain.player.service.impl.SignService;
import org.codequistify.master.domain.player.service.impl.VerifyMailService;
import org.codequistify.master.global.jwt.TokenProvider;
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
@Tag(name = "Sign")
@RequestMapping("api")
public class SignController {
    private final Logger LOGGER = LoggerFactory.getLogger(SignController.class);
    private final GoogleSocialSignService googleSocialSignService;
    private final KakaoSocialSignService kakaoSocialSignService;
    private final SignService signService;
    private final VerifyMailService verifyMailService;
    private final TokenProvider tokenProvider;

    @Operation(
            summary = "구글 로그인 url 발급",
            description = "구글 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @GetMapping("oauth2/google-url")
    public String loginUrlGoogle() {
        return googleSocialSignService.getSocialSignInURL();
    }

    @Operation(
            summary = "구글 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 'email'과 'name'을 반환받는다."
    )
    @PostMapping("oauth2/google")
    public ResponseEntity<SignInResponse> socialSignInGoogle(@RequestBody SignRequest request, HttpServletResponse response) {
        if (request.code().isBlank()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        SignInResponse signInResponse = googleSocialSignService.socialLogin(request.code());

        String refreshToken = tokenProvider.generateRefreshToken(signInResponse);
        String accessToken = tokenProvider.generateAccessToken(signInResponse);

        addAccessTokensToCookies(accessToken, response);
        addRefreshTokensToCookies(refreshToken, response);
        signService.updateRefreshToken(signInResponse.id(), refreshToken); // refresh token db에 저장

        LOGGER.info("[socialSignInGoogle] {} 구글 로그인", signInResponse.email());
        return new ResponseEntity<>(signInResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "토큰 재발급",
            description = "AccessToken을 재발급 한다. 재발급 받을 player의 id를 경로로 받는다.\n\n" +
                    " RefreshToken 값을 body로 받는다. 이때 token은 'bearer' 없이 token 값만을 적어야 한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상적으로 재발급"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청, id 불일치 또는 빈 요청 등"),
                    @ApiResponse(responseCode = "401", description = "만료된 Refresh Token")
            }
    )
    @PostMapping("refresh/id/{id}")
    public ResponseEntity<TokenResponse> regenerateAccessToken(@RequestBody TokenRequest request, @PathVariable Long id, HttpServletResponse response) {
        if (request.refreshToken().isBlank()) {
            LOGGER.info("[regenerateAccessToken] {} 빈 요청", id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Claims claims = tokenProvider.getClaims(request.refreshToken());
        if (!claims.get("id").equals(id)) {
            LOGGER.info("[regenerateAccessToken] 일치하지 않는 id {}:{}", claims.get("id"), id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!tokenProvider.checkExpire(claims)) {
            LOGGER.info("[regenerateAccessToken] 만료된 refresh token {}", id);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String accessToken = tokenProvider.generateAccessToken(new SignInResponse(id,
                claims.get("email").toString(),
                claims.get("name").toString(),
                null));

        addAccessTokensToCookies(accessToken, response);
        TokenResponse tokenResponse = new TokenResponse(request.refreshToken(), accessToken);
        LOGGER.info("[regenerateAccessToken] {} AccessToken 재발급", id);

        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

    private void addAccessTokensToCookies(String accessToken, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60); // 한 시간

        response.addCookie(accessTokenCookie);
    }

    private void addRefreshTokensToCookies(String refreshToken, HttpServletResponse response) {

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(24 * 60 * 60); // 하루

        response.addCookie(refreshTokenCookie);
    }


    @Operation(
            summary = "TEST 구글 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 'email'과 'name'을 반환받는다."
    )
    @PostMapping("oauth2/google/TEST")
    public ResponseEntity<SignInResponse> TEST_socialSignInGoogle(@RequestBody SignRequest request, HttpServletResponse response) {
        if (request.code().isBlank()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        SignInResponse signInResponse = googleSocialSignService.socialLogin(request.code());

        String refreshToken = tokenProvider.generateRefreshToken(signInResponse);
        String accessToken = tokenProvider.generateAccessToken(signInResponse);

        addAccessTokensToCookies(accessToken, response);
        addRefreshTokensToCookies(refreshToken, response);
        signService.updateRefreshToken(signInResponse.id(), refreshToken); // refresh token db에 저장

        LOGGER.info("[TEST_socialSignInGoogle] {} 구글 로그인", signInResponse.email());
        return new ResponseEntity<>(signInResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "카카오 로그인 url 발급",
            description = "카카오 로그인 화면으로 넘어갈 수 있는 url을 발급한다. 고정값이며 저장해여 사용할 수 있다."
    )
    @GetMapping("oauth2/kakao-url")
    public String loginUrlKakao() {
        return kakaoSocialSignService.getSocialSignInURL();
    }

    @Operation(
            summary = "카카오 로그인 요청",
            description = "redirect로 받은 code를 인자로 전달한다. 유효한 code라면 사용자 'email'과 'knickname'을 반환받는다."
    )
    @PostMapping("oauth2/kakao")
    public ResponseEntity<SignInResponse> socialSignInKakao(@RequestBody SignRequest request) {
        if (request.code().isBlank()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        SignInResponse signInResponse = kakaoSocialSignService.socialLogin(request.code());

        LOGGER.info("{} kakao 로그인", signInResponse.email());
        return new ResponseEntity<>(signInResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "POL 자체 회원가입",
            description = "자체 회원가입이다. name, email, password를 필수로 입력받는다."
    )
    @PostMapping("sign-up/pol")
    public ResponseEntity<SignInResponse> polSignUp(@RequestBody SignRequest request, HttpServletResponse httpServletResponse){
        if(request.name().isBlank() || request.email().isBlank() || request.password().isBlank()){
            throw new IllegalArgumentException("email 또는 password, name이 비어있습니다.");
        }

        SignInResponse signInResponse = signService.signUp(request);

        String refreshToken = tokenProvider.generateRefreshToken(signInResponse);
        String accessToken = tokenProvider.generateAccessToken(signInResponse);

        addAccessTokensToCookies(accessToken, httpServletResponse);
        addRefreshTokensToCookies(refreshToken, httpServletResponse);
        signService.updateRefreshToken(signInResponse.id(), refreshToken); // refresh token db에 저장

        LOGGER.info("[polSignUp] {} pol 회원가입 ", signInResponse.id());
        return new ResponseEntity<>(signInResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "POL 자체 로그인",
            description = "자체 로그인기능이다. name, password를 필수로 입력받는다."
    )
    @PostMapping("sign-in/pol")
    public ResponseEntity<SignInResponse> polSignIn(@RequestBody SignRequest request, HttpServletResponse httpServletResponse) {
        if (request.email().isBlank() || request.password().isBlank()){
            throw new IllegalArgumentException("email 또는 password가 비어있습니다.");
        }

        SignInResponse signInResponse = signService.signIn(request);

        String refreshToken = tokenProvider.generateRefreshToken(signInResponse);
        String accessToken = tokenProvider.generateAccessToken(signInResponse);

        addAccessTokensToCookies(accessToken, httpServletResponse);
        addRefreshTokensToCookies(refreshToken, httpServletResponse);
        signService.updateRefreshToken(signInResponse.id(), refreshToken); // refresh token db에 저장


        LOGGER.info("[polSignIn] {} pol 로그인", signInResponse.id());
        return new ResponseEntity<>(signInResponse, HttpStatus.OK);
    }

    @Operation(
            summary = "로그아웃 요청",
            description = "id를 인자로 받는다. Authorization에 토큰이 있어야 한다.",
            responses = {
                @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
                @ApiResponse(responseCode = "401", description = "잘못된 토큰 정보")
            }
    )
    @PostMapping("log-out")
    public ResponseEntity<Void> LogOut(@RequestBody LogOutRequest request, HttpServletRequest httpServletRequest) {
        String token = tokenProvider.resolveToken(httpServletRequest);


        if (token == null) {
            LOGGER.info("[LogOut] 빈 token");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Claims claims = tokenProvider.getClaims(token);
        if (claims == null) {
            LOGGER.info("[LogOut] 유효하지 않은 token");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        signService.LogOut(request, token);
        LOGGER.info("[LogOut] {} 로그아웃 완료", request.id());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
    @GetMapping("sign-up/email/{email}")
    public ResponseEntity<BasicResponse> checkEmailDuplication(@PathVariable String email){
        if (signService.checkEmailDuplication(email)){
            BasicResponse response = new BasicResponse(null, "Already Exist This Email");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }else {
            BasicResponse response = new BasicResponse("Non Exist This Email", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @Operation(
            summary = "회원가입 인증메일 발송",
            description = "회원가입 인증메일을 발송하는 요청이다. 응답값이 존재하지 않는 요청이다."
    )
    @GetMapping("sign-up/email/{email}/verify")
    public ResponseEntity<Void> sendAuthMail(@PathVariable String email) throws MessagingException {
        verifyMailService.sendVerifyMail(email);

        LOGGER.info("[sendAuthMail] {} 인증 메일 전송", email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "회원가입 인증코드 확인",
            description = "회원가입 인증코드를 확인한다. 사용자의 email과 입력 코드를 path로 받는다. 결과는 response 필드에 \"true\", \"false\"로 반환한다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "인증 결과",
                            content = @Content(
                                    schema = @Schema(implementation = BasicResponse.class),
                                    examples = {
                                            @ExampleObject(name = "올바른 코드", value = "{\"response\":\"true\"}"),
                                            @ExampleObject(name = "잘못된 코드", value = "{\"response\":\"false\"}")}))}
    )
    @GetMapping("sign-up/email/{email}/code/{code}")
    public ResponseEntity<BasicResponse> verifyCode(@PathVariable String email, @PathVariable String code){
        code = code.trim();

        BasicResponse response = new BasicResponse(Boolean.toString(verifyMailService.checkValidCode(email, code)), null);

        LOGGER.info("[verifyCode] {} 회원가입 메일 코드 인증 {}", email, response.response());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }





    //서버용인증
    @GetMapping("oauth2/google")
    @Operation(hidden = true)
    public SignInResponse googleLogin(@RequestParam String code) {
        SignInResponse response = googleSocialSignService.socialLogin(code);

        return response;
    }
    //서버용인증
    @Operation(hidden = true)
    @GetMapping("oauth2/kakao")
    public SignInResponse kakaoLogin(@RequestParam String code) {
        SignInResponse response = kakaoSocialSignService.socialLogin(code);

        return response;
    }

}
