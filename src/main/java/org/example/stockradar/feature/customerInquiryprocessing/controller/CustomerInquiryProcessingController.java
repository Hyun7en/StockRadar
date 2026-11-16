package org.example.stockradar.feature.customerInquiryprocessing.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockradar.feature.auth.service.CustomUserDetailsService;
import org.example.stockradar.feature.customerInquiryprocessing.dto.CustomerInquiryProcessingRequestDto;
import org.example.stockradar.feature.customerInquiryprocessing.dto.CustomerInquiryProcessingResponseDto;
import org.example.stockradar.feature.customerInquiryprocessing.service.CustomerInquiryProcessingService;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("customerInquiryprocessing")
@RequiredArgsConstructor
public class CustomerInquiryProcessingController {
    private final CustomerInquiryProcessingService service;
    private final CustomUserDetailsService customUserDetailsService;

    @GetMapping("customerInquiryprocessing")
    public String getCustomerInquiryProcessing() {
        return "customerInquiryprocessing/customerInquiryprocessing";
    }

    @GetMapping("api/customerInquiryprocessing")
    public ResponseEntity<ApiResponse<List<CustomerInquiryProcessingResponseDto>>> getApiCustomerInquiryProcessing() {
        requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok(service.getInqueryList()));
    }

    @GetMapping("detail/{inquiryId}")
    public String getCustomerInquiryDetail() {

        // 고정된 템플릿 경로 반환
        return "customerInquiryprocessing/detail";
    }

    @GetMapping("api/detail/{inquiryId}")
    public ResponseEntity<ApiResponse<CustomerInquiryProcessingResponseDto>> getApiCustomerInquiryDetail(@PathVariable Long inquiryId) {
        requireAdmin();
        return ResponseEntity.ok(ApiResponse.ok(service.getInquiryById(inquiryId)));
    }

    @PostMapping("process")
    public String processCustomerInquiry(
            @RequestParam("inquiryId") Long inquiryId,
            @RequestParam("processingTitle") String processingTitle,
            @RequestParam("processingContent") String processingContent) {
        requireAdmin();

        CustomerInquiryProcessingRequestDto requestDto = CustomerInquiryProcessingRequestDto.builder()
                .processingTitle(processingTitle)
                .processingContent(processingContent)
                .build();
        service.processInquiry(inquiryId, requestDto);

        // 성공 메시지와 함께 목록 페이지로 리다이렉트
        return "redirect:/customerInquiryprocessing/customerInquiryprocessing?success=true";
    }

    private void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (!"ADMIN".equals(customUserDetailsService.getMemberRole(auth.getName()))) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
