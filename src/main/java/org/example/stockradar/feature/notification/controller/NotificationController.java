package org.example.stockradar.feature.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockradar.feature.auth.entity.Member;
import org.example.stockradar.feature.auth.repository.MemberRepository;
import org.example.stockradar.feature.notification.dto.InterestProductRequestDto;
import org.example.stockradar.feature.notification.dto.InterestProductResponseDto;
import org.example.stockradar.feature.notification.dto.NotificationSettingsDto;
import org.example.stockradar.feature.notification.service.InterestProductService;
import org.example.stockradar.feature.notification.service.NotificationService;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * @author Hyun7en
 */

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final InterestProductService interestProductService;
    private final MemberRepository memberRepository;

    /*
     * 관심 상품 등록 + 알림 전송
     * 예시: 사용자가 관심 상품을 등록하면 해당 정보를 저장하고,
     * 등록 완료 후 알림 이벤트를 생성하여 선택된 채널로 알림을 발송합니다.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerInterestProduct(@RequestBody InterestProductRequestDto request, Authentication authentication) {
        String memberId = requireLogin(authentication);

        // 현재 로그인한 회원 정보 조회
        Member member = memberRepository.findByMemberId(memberId);
        // 관심 상품 등록 (동기 처리)
        Long interestProductId = interestProductService.registerInterestProduct(request, member);
        // 관심 상품 등록 후 알림 전송 (비동기 처리)
        notificationService.dispatchNotificationForInterestProduct(interestProductId, request, memberId);
        return ResponseEntity.ok(ApiResponse.ok(null, "관심 상품 등록 및 알림 전송 요청 완료"));
    }

    /*
     * 관심 상품 조회
     * 예시: 특정 회원의 관심 상품 목록을 조회합니다.
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<Page<InterestProductResponseDto>>> getInterestProducts(Authentication authentication, @RequestParam(defaultValue = "0") int page) {
        String memberId = requireLogin(authentication);
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(interestProductService.getInterestProductsByMemberId(memberId, pageable)));
    }

    /*
     * 관심 상품 삭제 + 알림 삭제
     * 예시: 관심 상품 삭제 시 해당 관심 상품과 관련된 알림도 함께 삭제합니다.
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteInterestProduct(@RequestBody InterestProductRequestDto request, Authentication authentication) {
        String memberId = requireLogin(authentication);
        Long interestProductId = interestProductService.deleteInterestProduct(request.getProductId(), memberId);
        // 관심 상품과 연관된 알림도 삭제 처리
        notificationService.deleteNotificationsByInterestProductId(interestProductId, memberId);
        return ResponseEntity.ok(ApiResponse.ok(null, "관심 상품 및 관련 알림 삭제 완료"));
    }

    /**
     * 사용자 알림 채널 설정
     * 예시: 사용자가 재고 변경을 수신 받을 알림 채널을 설정합니다.
     */
    @PostMapping("/saveSettings")
    public ResponseEntity<ApiResponse<Void>> updateSettings(@RequestBody NotificationSettingsDto settingsDto, Authentication authentication) {
        log.info("settingDTo{}", settingsDto);
        String memberId = requireLogin(authentication);
        notificationService.updateSettings(memberId, settingsDto);
        return ResponseEntity.ok(ApiResponse.ok(null, "알림 설정이 업데이트되었습니다."));
    }

    /**
     * 사용자 알림 채널 불러오기
     * 예시: 사용자가 설정한 알림 채널을 화면에 고정시키기 위해 사용합니다.
     */
    @GetMapping("/readSettings")
    public ResponseEntity<ApiResponse<NotificationSettingsDto>> readSettings(Authentication authentication) {
        String memberId = requireLogin(authentication);
        NotificationSettingsDto settings = notificationService.getNotificationSettings(memberId);
        log.info("settings{}", settings);
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    private String requireLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인 해주세요.");
        }
        return authentication.getName();
    }
}
