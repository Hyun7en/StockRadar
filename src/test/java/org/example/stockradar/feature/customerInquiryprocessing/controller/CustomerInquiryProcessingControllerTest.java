package org.example.stockradar.feature.customerInquiryprocessing.controller;

import org.example.stockradar.feature.auth.service.CustomUserDetailsService;
import org.example.stockradar.feature.customerInquiryprocessing.service.CustomerInquiryProcessingService;
import org.example.stockradar.feature.customerInquiryprocessing.dto.CustomerInquiryProcessingResponseDto;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerInquiryProcessingControllerTest {

    private final CustomerInquiryProcessingService service = mock(CustomerInquiryProcessingService.class);
    private final CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerInquiryProcessingController(service, userDetailsService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String memberId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(memberId, null, List.of()));
        given(userDetailsService.getMemberRole(memberId)).willReturn(role);
    }

    @Test
    void 관리자가_아니면_403() throws Exception {
        loginAs("user1", "USER");
        mockMvc.perform(get("/customerInquiryprocessing/api/customerInquiryprocessing"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.FORBIDDEN.getErrorCode()));
    }

    @Test
    void 비로그인이면_401() throws Exception {
        mockMvc.perform(get("/customerInquiryprocessing/api/customerInquiryprocessing"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 관리자_목록조회는_data에_목록을_담는다() throws Exception {
        loginAs("admin", "ADMIN");
        given(service.getInqueryList()).willReturn(List.of(
                CustomerInquiryProcessingResponseDto.builder().inquiryId(3L).inquiryTitle("문의").build()));

        mockMvc.perform(get("/customerInquiryprocessing/api/customerInquiryprocessing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].inquiryId").value(3));
    }

    @Test
    void 없는_문의_상세는_404() throws Exception {
        loginAs("admin", "ADMIN");
        given(service.getInquiryById(99L)).willThrow(new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

        mockMvc.perform(get("/customerInquiryprocessing/api/detail/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INQUIRY_NOT_FOUND.getErrorCode()));
    }

    @Test
    void 서비스에서_예상못한_예외가_나면_404가_아니라_500() throws Exception {
        loginAs("admin", "ADMIN");
        given(service.getInqueryList()).willThrow(new IllegalStateException("DB down"));
        mockMvc.perform(get("/customerInquiryprocessing/api/customerInquiryprocessing"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode()));
    }
}
