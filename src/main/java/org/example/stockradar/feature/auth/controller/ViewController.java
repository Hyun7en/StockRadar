package org.example.stockradar.feature.auth.controller;

import org.example.stockradar.feature.CustomerInquiry.controller.CustomerInquiryController;
import org.example.stockradar.feature.auth.dto.ViewResponseDto;
import org.example.stockradar.feature.auth.service.CustomUserDetailsService;
import org.example.stockradar.global.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    @Autowired
    CustomUserDetailsService customUserDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    // 로그인 폼
    @GetMapping("/login")
    public String loginForm() {
        return "auth/signIn"; // login.html (Thymeleaf)
    }

    // 회원가입 폼
    @GetMapping("auth/signUp")
    public String signupForm() {
        return "auth/signUp"; // signup.html
    }

    // 메인 페이지
    @GetMapping("/main")
    public String main() {
        return "/main";
    }
    @GetMapping("/main/check")
    public ResponseEntity<ApiResponse<ViewResponseDto>> mainPage(Authentication authentication) {
        ViewResponseDto viewResponseDto;

        if (authentication != null && authentication.isAuthenticated()) {
            String memberId = authentication.getName();
            String role = customUserDetailsService.getMemberRole(memberId);
            logger.info("User {} has role: {}", memberId, role);
            viewResponseDto = ViewResponseDto.builder()
                    .memberId(memberId)
                    .role(role)
                    .redirectUrl("/main")
                    .build();
        } else {
            logger.info("Unauthenticated user accessing main page");
            viewResponseDto = ViewResponseDto.builder()
                    .role("GUEST")
                    .redirectUrl("/main")
                    .build();
        }

        return ResponseEntity.ok(ApiResponse.ok(viewResponseDto));
    }




    // 아이디 찾기 폼
    @GetMapping("auth/idInquiry")
    public String idInquiryForm() {
        return "auth/idInquiry"; // idInquiry.html
    }

    @GetMapping("auth/pwInquiry")
    public String redirectPwInquiry() {
        return "redirect:/password/pwInquiry";
    }

}
