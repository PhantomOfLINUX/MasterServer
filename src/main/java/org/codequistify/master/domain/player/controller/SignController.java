package org.codequistify.master.domain.player.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.dto.SignRequest;
import org.codequistify.master.domain.player.service.impl.GoogleSocialSignService;
import org.codequistify.master.domain.player.service.impl.KakaoSocialSignService;
import org.codequistify.master.domain.player.service.impl.SignService;
import org.codequistify.master.domain.player.service.impl.VerifyMailService;
import org.codequistify.master.global.util.BasicResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Player")
@RequestMapping("api")
public class SignController {
    private final Logger LOGGER = LoggerFactory.getLogger(SignController.class);
    private final GoogleSocialSignService googleSocialSignService;
    private final KakaoSocialSignService kakaoSocialSignService;
    private final SignService signService;
    private final VerifyMailService verifyMailService;

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
    public ResponseEntity<PlayerDTO> socialSignInGoogle(@RequestBody SignRequest request) {
        if (request.code().isBlank()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PlayerDTO playerDTO = googleSocialSignService.socialLogin(request.code());

        LOGGER.info("{} google 로그인", playerDTO.id());
        return new ResponseEntity<>(playerDTO, HttpStatus.OK);
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
    public ResponseEntity<PlayerDTO> socialSignInKakao(@RequestBody SignRequest request) {
        if (request.code().isBlank()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PlayerDTO playerDTO = kakaoSocialSignService.socialLogin(request.code());

        LOGGER.info("{} kakao 로그인", playerDTO.id());
        return new ResponseEntity<>(playerDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "POL 자체 회원가입",
            description = "자체 회원가입이다. name, email, password를 필수로 입력받는다."
    )
    @PostMapping("sign-up/pol")
    public ResponseEntity<PlayerDTO> polSignUp(@RequestBody SignRequest request){
        if(request.name().isBlank() || request.email().isBlank() || request.password().isBlank()){
            throw new IllegalArgumentException("email 또는 password, name이 비어있습니다.");
        }

        PlayerDTO playerDTO = signService.signUp(request);

        LOGGER.info("{} pol 로그인", playerDTO.id());
        return new ResponseEntity<>(playerDTO, HttpStatus.OK);
    }

    @Operation(
            summary = "POL 자체 로그인",
            description = "자체 로그인기능이다. name, password를 필수로 입력받는다."
    )
    @PostMapping("sign-in/pol")
    public ResponseEntity<PlayerDTO> polSignIn(@RequestBody SignRequest request){
        if (request.email().isBlank() || request.password().isBlank()){
            throw new IllegalArgumentException("email 또는 password가 비어있습니다.");
        }

        PlayerDTO playerDTO = signService.signIn(request);

        LOGGER.info("{} pol 로그인", playerDTO.id());
        return new ResponseEntity<>(playerDTO, HttpStatus.OK);
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
    @PostMapping("sign-up/email/{email}")
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
    public PlayerDTO googleLogin(@RequestParam String code) {
        PlayerDTO playerDTO = googleSocialSignService.socialLogin(code);

        return playerDTO;
    }
    //서버용인증
    @Operation(hidden = true)
    @GetMapping("oauth2/kakao")
    public PlayerDTO kakaoLogin(@RequestParam String code) {
        PlayerDTO playerDTO = kakaoSocialSignService.socialLogin(code);

        return playerDTO;
    }

}
