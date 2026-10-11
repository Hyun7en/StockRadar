package org.example.stockradar.feature.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.stockradar.feature.notification.entity.NotificationChannel;

import java.util.List;

/**
 * @author Hyun7en
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Long interestProductId;           // 관심 상품 ID
    private List<NotificationChannel> channels; // 선택된 채널 목록
    @lombok.ToString.Exclude
    private String memberId;                    // 수신 회원 식별자 (연락처는 consumer에서 DB 조회)

    // 연락처(개인정보)는 Kafka 메시지에 싣지 않고(@JsonIgnore), 로그에도 남기지 않음(@ToString.Exclude)
    @JsonIgnore @lombok.ToString.Exclude
    private String phoneNumber;                 // SMS 전송용 전화번호
    @JsonIgnore @lombok.ToString.Exclude
    private String emailAddress;                // 이메일 전송용 주소
    @JsonIgnore @lombok.ToString.Exclude
    private String discordUserId;               // 디스코드 DM 전송용 사용자 ID
    private String messageContent;              // 기본 메시지 내용
    // 추가 정보
    private String productName;                 // 상품 이름
    private String productUrl;                  // 상품 URL
    private String stockStatus;                 // 재고 상태 (재고 변경 알림 시 사용)
    private String notificationType;            // 알림 유형 ("registration", "stockChange" 등)
}
