package org.example.stockradar.config;

import org.example.stockradar.feature.auth.jwt.JwtAuthenticationFilter;
import org.example.stockradar.feature.auth.service.CustomUserDetailsService;
import org.example.stockradar.feature.auth.service.OAuth2LoginSuccessHandler;
import org.example.stockradar.feature.auth.service.RoutingOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RoutingOAuth2UserService routingOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager (필요 시)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers(headers -> headers
                        .cacheControl(cache -> cache.disable())  // 기본 캐시 제어 헤더 비활성화
                        .frameOptions(frame -> frame.deny())     // 프레임 옵션 유지
                )

                // .authenticationManager(authenticationManager)

                // CSRF 토큰은 사용하지 않음: 인증 쿠키를 SameSite=Lax로 발급해 교차 사이트 POST에 쿠키가 실리지 않도록 함
                .authorizeHttpRequests(auth -> auth
                        // 공개 페이지 / 정적 리소스
                        .requestMatchers(
                                "/", "/error", "/main", "/main/check", "/login", "/signup", "/idInquiry",
                                "/auth/**", "/password/**",
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/ajax/**",
                                "/common/**", "/product/**", "/customerInquiry/**"
                        ).permitAll()
                        // 게시판/댓글은 조회만 공개, 작성·수정·삭제는 로그인 필요
                        .requestMatchers(HttpMethod.GET,
                                "/board/list", "/board/detail", "/board/search", "/comment/read"
                        ).permitAll()
                        // 관리자 전용
                        .requestMatchers("/monitoring/**", "/customerInquiryprocessing/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // AJAX(JSON) 엔드포인트는 로그인 페이지 리다이렉트 대신 401 반환
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new OrRequestMatcher(
                                        new AntPathRequestMatcher("/comment/**"),
                                        new AntPathRequestMatcher("/board/delete"),
                                        new AntPathRequestMatcher("/notification/**", HttpMethod.POST.name()),
                                        new AntPathRequestMatcher("/notification/read"),
                                        new AntPathRequestMatcher("/notification/readSettings"),
                                        new AntPathRequestMatcher("/monitoring/**")
                                ))
                )

                .oauth2Login(oauth2 -> oauth2

                        .loginPage("/login") // 커스텀 로그인 페이지
                        .userInfoEndpoint(userInfo -> userInfo.userService(routingOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}