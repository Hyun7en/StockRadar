package org.example.stockradar.feature.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.stockradar.feature.auth.dto.LoginRequest;
import org.example.stockradar.feature.auth.dto.MemberSignupDto;
import org.example.stockradar.feature.auth.entity.Member;
import org.example.stockradar.feature.auth.jwt.AuthCookieManager;
import org.example.stockradar.feature.auth.jwt.JwtTokenProvider;
import org.example.stockradar.feature.auth.jwt.RefreshTokenRedisService;
import org.example.stockradar.feature.auth.repository.MemberRepository;
import org.example.stockradar.feature.auth.service.CoolsmsService;
import org.example.stockradar.feature.auth.service.MemberService;
import org.example.stockradar.global.security.RedisRateLimiter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final CoolsmsService coolsmsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuthCookieManager authCookieManager;
    private final RedisRateLimiter rateLimiter;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String LOGIN_FAIL_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";
    private static final int LOGIN_MAX_FAILURES = 10;

    @PostMapping("/findIdByPhone")
    public String findIdByPhone(@RequestParam("phone") String phone, HttpServletRequest request, Model model) {
        if (!rateLimiter.tryAcquire("FIND_ID:" + request.getRemoteAddr(), 10, Duration.ofMinutes(10))) {
            model.addAttribute("error", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
            return "auth/idInquiry";
        }

        List<Member> members = memberRepository.findAllByMemberPhone(phone);
        if (members.isEmpty()) {
            model.addAttribute("error", "해당 번호로 등록된 아이디(이메일)가 없습니다.");
        } else {
            // 엔티티 대신 마스킹된 아이디만 전달
            model.addAttribute("foundMembers", members.stream().map(m -> maskId(m.getMemberId())).toList());
        }
        return "auth/idInquiry";
    }

    // abcdef@gmail.com → ab****@gmail.com
    private static String maskId(String memberId) {
        int at = memberId.indexOf('@');
        String local = at >= 0 ? memberId.substring(0, at) : memberId;
        String domain = at >= 0 ? memberId.substring(at) : "";
        int visible = Math.min(2, local.length());
        return local.substring(0, visible) + "****" + domain;
    }

    @PostMapping("/sendCode")
    public String sendCode(@RequestParam("memberPhone") String memberPhone, @RequestParam("memberId") String memberId,
                           @RequestParam("memberPw") String memberPw, @RequestParam("userName") String userName,
                           HttpServletRequest request, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("memberPw", memberPw);
        model.addAttribute("userName", userName);
        model.addAttribute("memberPhone", memberPhone);

        if (memberPhone.isEmpty()) {
            model.addAttribute("error", "휴대폰 번호를 입력해주세요.");
            return "auth/signUp";
        }

        // SMS 발송 남용(요금 소모·문자 폭탄) 방지: 번호당 5회/시간, IP당 20회/시간
        if (!rateLimiter.tryAcquire("SMS_PHONE:" + memberPhone, 5, Duration.ofHours(1))
                || !rateLimiter.tryAcquire("SMS_IP:" + request.getRemoteAddr(), 20, Duration.ofHours(1))) {
            model.addAttribute("error", "인증번호 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
            return "auth/signUp";
        }

        String code = String.valueOf(SECURE_RANDOM.nextInt(900000) + 100000);
        redisTemplate.opsForValue().set("PHONE_VERIFY:" + memberPhone, code, Duration.ofMinutes(3));
        rateLimiter.reset("SMS_VERIFY:" + memberPhone); // 새 코드 발급 시 대입 시도 횟수 초기화
        coolsmsService.sendSms(memberPhone, "[StockRadar] 인증번호: " + code);

        model.addAttribute("success", "인증번호를 전송했습니다. (3분 내 입력)");
        return "auth/signUp";
    }

    @PostMapping("/checkCode")
    public String checkCode(@RequestParam("memberPhone") String memberPhone, @RequestParam("verifyCode") String verifyCode,
                            @RequestParam("memberId") String memberId, @RequestParam("memberPw") String memberPw,
                            @RequestParam("userName") String userName, HttpServletRequest request, Model model) {
        model.addAttribute("memberId", memberId);
        model.addAttribute("memberPw", memberPw);
        model.addAttribute("userName", userName);
        model.addAttribute("memberPhone", memberPhone);

        // 6자리 코드 대입 방지: 번호당 5회 실패 시 해당 코드 폐기
        if (!rateLimiter.tryAcquire("SMS_VERIFY:" + memberPhone, 5, Duration.ofMinutes(3))) {
            redisTemplate.delete("PHONE_VERIFY:" + memberPhone);
            model.addAttribute("error", "인증 시도가 너무 많습니다. 인증번호를 다시 요청해주세요.");
            return "auth/signUp";
        }

        String savedCode = redisTemplate.opsForValue().get("PHONE_VERIFY:" + memberPhone);
        if (savedCode == null || !savedCode.equals(verifyCode)) {
            model.addAttribute("error", "인증번호가 일치하지 않거나 만료되었습니다.");
            return "auth/signUp";
        }
        redisTemplate.delete("PHONE_VERIFY:" + memberPhone);
        rateLimiter.reset("SMS_VERIFY:" + memberPhone);
        request.getSession().setAttribute("phoneVerified", true);
        model.addAttribute("success", "휴대폰 인증이 완료되었습니다!");
        return "auth/signUp";
    }

    @PostMapping("/signup")
    public String signup(MemberSignupDto dto,
                         HttpServletRequest request,
                         Model model) {
        try {
            // 휴대폰 인증 여부 확인
            Boolean phoneVerified = (Boolean) request.getSession().getAttribute("phoneVerified");
            if (phoneVerified == null || !phoneVerified) {
                model.addAttribute("error", "휴대폰 인증이 완료되지 않았습니다.");

                // 기존 입력값 유지
                model.addAttribute("memberId", dto.getMemberId());
                model.addAttribute("memberPw", dto.getMemberPw());
                model.addAttribute("userName", dto.getUserName());
                model.addAttribute("memberPhone", dto.getMemberPhone());
                return "auth/signUp";
            }

            // 실제 회원가입 로직
            memberService.signUp(dto);

            // 가입 완료 후 세션에서 제거
            request.getSession().removeAttribute("phoneVerified");

            model.addAttribute("msg", "회원가입이 완료되었습니다.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            // 가입 중 오류 발생 시
            model.addAttribute("error", e.getMessage());

            // 기존 입력값 유지
            model.addAttribute("memberId", dto.getMemberId());
            model.addAttribute("memberPw", dto.getMemberPw());
            model.addAttribute("userName", dto.getUserName());
            model.addAttribute("memberPhone", dto.getMemberPhone());
            return "auth/signUp";
        }
    }

    @PostMapping("/login")
    public String login(LoginRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        String failKey = "LOGIN_FAIL:" + request.getMemberId();
        if (rateLimiter.isBlocked(failKey, LOGIN_MAX_FAILURES)) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 시도가 너무 많습니다. 15분 후 다시 시도해주세요.");
            return "redirect:/login";
        }

        Member member = memberRepository.findByMemberId(request.getMemberId());
        // 아이디 존재 여부가 드러나지 않도록 동일한 메시지 사용
        if (member == null || !passwordEncoder.matches(request.getMemberPw(), member.getMemberPw())) {
            rateLimiter.tryAcquire(failKey, LOGIN_MAX_FAILURES, Duration.ofMinutes(15));
            redirectAttributes.addFlashAttribute("errorMessage", LOGIN_FAIL_MESSAGE);
            return "redirect:/login";
        }
        rateLimiter.reset(failKey);

        String accessToken = jwtTokenProvider.generateAccessToken(member.getMemberId());
        String sessionId = UUID.randomUUID().toString();

        // SESSION_ID → memberId 를 Redis에 저장 (refresh 용도)
        refreshTokenRedisService.saveSession(sessionId, member.getMemberId(), jwtTokenProvider.getRefreshTokenValidity());

        authCookieManager.addCookie(response, AuthCookieManager.ACCESS_TOKEN, accessToken,
                Duration.ofMillis(jwtTokenProvider.getAccessTokenValidity()));
        authCookieManager.addCookie(response, AuthCookieManager.SESSION_ID, sessionId,
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity()));
        return "redirect:/";
    }

    @ResponseBody
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@CookieValue(value = "SESSION_ID", required = false) String sessionId,
                                                       HttpServletResponse response) {
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "세션이 없습니다."));
        }

        String memberId = refreshTokenRedisService.getMemberId(sessionId);
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "세션이 만료되었습니다."));
        }

        // 새 AccessToken은 HttpOnly 쿠키로만 전달 (응답 본문에 토큰 노출 X)
        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId);
        authCookieManager.addCookie(response, AuthCookieManager.ACCESS_TOKEN, newAccessToken,
                Duration.ofMillis(jwtTokenProvider.getAccessTokenValidity()));

        return ResponseEntity.ok(Map.of("result", "refreshed"));
    }

    @GetMapping("/logout")
    public String logout(
            @CookieValue(value = "SESSION_ID", required = false) String sessionId,
            HttpServletResponse response) {

        // Redis에 저장된 세션 삭제
        if (sessionId != null) {
            refreshTokenRedisService.deleteRefreshToken(sessionId);
        }

        authCookieManager.deleteCookie(response, AuthCookieManager.SESSION_ID);
        authCookieManager.deleteCookie(response, AuthCookieManager.ACCESS_TOKEN);

        return "redirect:/login";
    }
}
