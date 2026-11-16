package org.example.stockradar.feature.CustomerInquiry.service;

import lombok.RequiredArgsConstructor;
import org.example.stockradar.feature.CustomerInquiry.dto.CustomerInquiryResponseDto;
import org.example.stockradar.feature.CustomerInquiry.dto.CustomerInquiryUserRequestDto;
import org.example.stockradar.feature.CustomerInquiry.entity.CustomerInquiry;
import org.example.stockradar.feature.CustomerInquiry.repository.CustomerInquiryRepository;
import org.example.stockradar.feature.auth.entity.Member;
import org.example.stockradar.feature.auth.repository.MemberRepository;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.example.stockradar.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class CustomerInquiryService {
    private final CustomerInquiryRepository repository;
    private final MemberRepository memberRepository;

    public CustomerInquiryResponseDto saveCustomerInquiry(CustomerInquiryUserRequestDto requestDto, String memberId) {
        // 현재 로그인한 회원 정보 조회
        Member member = memberRepository.findByMemberId(memberId);

        // 회원이 없는 경우 예외 처리
        if (member == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            CustomerInquiry inquiry = CustomerInquiry.builder()
                    .inquiryTitle(requestDto.getTitle())
                    .inquiryCategory(requestDto.getCategory())
                    .inquiryContent(requestDto.getContent())
                    .inquiryStatus(0)
                    .member(member) // 회원 정보 설정
                    .build();
            Long inquiryId = repository.save(inquiry).getInquiryId();

            return CustomerInquiryResponseDto.builder()
                    .inquiryId(inquiryId)
                    .redirectUrl("/customerInquiry/customerInquiry")
                    .build();
        }
        //데이터 저장 실패
        catch (Exception e) {
            throw new BusinessException(ErrorCode.RESOURCE_SAVE_FAILED);
        }


    }
}