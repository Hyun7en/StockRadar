package org.example.stockradar.feature.auth.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.example.stockradar.feature.auth.jwt.AuthCookieManager;
import org.example.stockradar.feature.auth.jwt.JwtTokenProvider;
import org.example.stockradar.feature.auth.jwt.RefreshTokenRedisService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final AuthCookieManager authCookieManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1) OAuth2User
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2) 모든 소셜(Naver/Discord)에서 "memberId"를 넣어두었으므로
        String memberId = (String) attributes.get("memberId");
        if (memberId == null) {
            response.sendRedirect("/login?error=NoMemberId");
            return;
        }

        // 3) JWT 생성 + 세션 ID 발급
        String accessToken = jwtTokenProvider.generateAccessToken(memberId);
        String sessionId = UUID.randomUUID().toString();

        // 4) Redis에 SESSION_ID → memberId 저장 (refresh 용도)
        refreshTokenRedisService.saveSession(sessionId, memberId, jwtTokenProvider.getRefreshTokenValidity());

        // 5) 쿠키 발급 (HttpOnly, Secure, SameSite=Lax)
        authCookieManager.addCookie(response, AuthCookieManager.ACCESS_TOKEN, accessToken,
                Duration.ofMillis(jwtTokenProvider.getAccessTokenValidity()));
        authCookieManager.addCookie(response, AuthCookieManager.SESSION_ID, sessionId,
                Duration.ofMillis(jwtTokenProvider.getRefreshTokenValidity()));

        // 6) 메인 페이지로 리다이렉트
        response.sendRedirect("/");
    }
}
