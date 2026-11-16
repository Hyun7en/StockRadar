package org.example.stockradar.feature.CustomerInquiry.controller;

import lombok.RequiredArgsConstructor;
import org.example.stockradar.feature.CustomerInquiry.dto.CustomerInquiryResponseDto;
import org.example.stockradar.feature.CustomerInquiry.dto.CustomerInquiryUserRequestDto;
import org.example.stockradar.feature.CustomerInquiry.service.CustomerInquiryService;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("customerInquiry")
@RequiredArgsConstructor
public class CustomerInquiryController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerInquiryController.class);
    private final CustomerInquiryService service;
    @GetMapping("customerInquiry")
    public String customerInquiryPage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
//            logger.warn("인증 실패.");
            return "redirect:/login";
        }

        // 인증된 사용자 ID 가져오기
        String memberId = authentication.getName();
//        logger.info("Authenticated user ID: {}", memberId);
        model.addAttribute("memberId", memberId);
        // customerInquiry.html 뷰 템플릿을 반환
        return "/customerInquiry/customerInquiry";
    }


    @GetMapping("/api/check")
    public ResponseEntity<ApiResponse<Void>> customerInquiry(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(ApiResponse.ok(null, "인증 성공"));
    }

    @PostMapping("submit")
    public ResponseEntity<ApiResponse<CustomerInquiryResponseDto>> submitCustomerInquiry(
            @RequestBody CustomerInquiryUserRequestDto requestDto,
            Authentication authentication) {
        if (authentication == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        CustomerInquiryResponseDto responseDto = service.saveCustomerInquiry(requestDto, authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(responseDto, "문의가 성공적으로 접수되었습니다."));
    }
}
