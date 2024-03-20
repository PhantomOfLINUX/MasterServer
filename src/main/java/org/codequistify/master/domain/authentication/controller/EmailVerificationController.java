package org.codequistify.master.domain.authentication.controller;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.domain.authentication.domain.EmailVerificationType;
import org.codequistify.master.domain.authentication.service.EmailVerificationService;
import org.codequistify.master.global.aspect.LogMonitoring;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;
    @GetMapping("home/auth/email/verify")
    @LogMonitoring
    public String verifyMail(@RequestParam String email, @RequestParam String code, @RequestParam EmailVerificationType type, Model model) {
        if (email.isBlank() || code.isBlank() || type == null) {
            return "redirect:/home/failure";
        }

        email = URLDecoder.decode(email, StandardCharsets.UTF_8);
        if (!emailVerificationService.verifyCode(email, code)) {
            return "redirect:/home/failure";
        }
        emailVerificationService.updateVerification(email, type); // 코드가 일치하면 인증 표시
        return "redirect:/home/success";
    }

    @GetMapping("home/success")
    public String success(Model model) {
        model.addAttribute("isValid", true);
        return "verification-complete";
    }

    @GetMapping("home/failure")
    public String failure(Model model) {
        model.addAttribute("isValid", false);
        return "verification-complete";
    }
}
