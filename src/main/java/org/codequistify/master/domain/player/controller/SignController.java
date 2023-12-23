package org.codequistify.master.domain.player.controller;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.player.dto.GoogleUserResource;
import org.codequistify.master.domain.player.service.SignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin("*")
@RequestMapping("api/sign")
public class SignController {
    private final Logger LOGGER = LoggerFactory.getLogger(SignController.class);
    private final SignService signService;

    @PostMapping("oauth2/google")
    public String loginUrlGoogle() {
        return signService.getSocialSignInURL();
    }

    @GetMapping("oauth2/google")
    public GoogleUserResource googleLogin(@RequestParam String code) {
        GoogleUserResource googleUserResource = signService.socialLogin(code);

        return googleUserResource;
    }
}
