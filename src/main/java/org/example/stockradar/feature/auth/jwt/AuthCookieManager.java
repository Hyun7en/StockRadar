package org.example.stockradar.feature.auth.jwt;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 인증 쿠키(ACCESS_TOKEN, SESSION_ID) 발급/삭제를 한 곳에서 처리.
 * HttpOnly + SameSite=Lax 고정, Secure는 app.cookie.secure 설정(운영 true).
 */
@Component
public class AuthCookieManager {

    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String SESSION_ID = "SESSION_ID";

    private final boolean secure;

    public AuthCookieManager(@Value("${app.cookie.secure:true}") boolean secure) {
        this.secure = secure;
    }

    public void addCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, "", Duration.ZERO);
    }
}
