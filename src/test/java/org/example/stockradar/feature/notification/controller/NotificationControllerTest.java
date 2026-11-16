package org.example.stockradar.feature.notification.controller;

import org.example.stockradar.feature.auth.repository.MemberRepository;
import org.example.stockradar.feature.notification.dto.NotificationSettingsDto;
import org.example.stockradar.feature.notification.service.InterestProductService;
import org.example.stockradar.feature.notification.service.NotificationService;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(
                        notificationService, mock(InterestProductService.class), mock(MemberRepository.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 알림설정_조회는_data에_설정을_담는다() throws Exception {
        NotificationSettingsDto settings = new NotificationSettingsDto();
        given(notificationService.getNotificationSettings("me")).willReturn(settings);

        mockMvc.perform(get("/notification/readSettings")
                        .principal(new UsernamePasswordAuthenticationToken("me", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void 비로그인_관심상품_조회는_401() throws Exception {
        mockMvc.perform(get("/notification/read"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNAUTHORIZED.getErrorCode()));
    }
}
