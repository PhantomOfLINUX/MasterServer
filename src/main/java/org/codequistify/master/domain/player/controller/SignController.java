package org.codequistify.master.domain.player.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.dto.PlayerDTO;
import org.codequistify.master.domain.player.dto.SignRequest;
import org.codequistify.master.domain.player.service.imple.GoogleSocialSignService;
import org.codequistify.master.domain.player.service.imple.KakaoSocialSignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Player")
@RequestMapping("api/sign")
public class SignController {
    private final Logger LOGGER = LoggerFactory.getLogger(SignController.class);
    private final GoogleSocialSignService googleSocialSignService;
    private final KakaoSocialSignService kakaoSocialSignService;

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




    //서버용인증
    @GetMapping("oauth2/google")
    public PlayerDTO googleLogin(@RequestParam String code) {
        PlayerDTO playerDTO = googleSocialSignService.socialLogin(code);

        return playerDTO;
    }
    //서버용인증
    @GetMapping("oauth2/kakao")
    public PlayerDTO kakaoLogin(@RequestParam String code) {
        PlayerDTO playerDTO = kakaoSocialSignService.socialLogin(code);

        return playerDTO;
    }

}
