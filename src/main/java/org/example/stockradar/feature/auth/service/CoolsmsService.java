package org.example.stockradar.feature.auth.service;

import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CoolsmsService {

    private final DefaultMessageService messageService;
    private final String sender;

    // API Key / Secret / 발신번호는 환경변수(COOLSMS_*)로 주입
    public CoolsmsService(@Value("${coolsms.api-key}") String apiKey,
                          @Value("${coolsms.api-secret}") String apiSecret,
                          @Value("${coolsms.sender}") String sender) {
        this.messageService = new DefaultMessageService(apiKey, apiSecret, "https://api.coolsms.co.kr");
        this.sender = sender;
    }

    /**
     * 단일 SMS를 전송합니다.
     *
     * @param to   수신자 번호
     * @param text 전송할 메시지 내용
     */
    public void sendSms(String to, String text) {
        Message message = new Message();
        message.setFrom(sender); // 인증된 발신번호
        message.setTo(to);
        message.setText(text);

        try {
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("CoolSMS 전송 완료: messageId={}", response.getMessageId());
        } catch (Exception e) {
            log.error("CoolSMS 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 관심 상품 등록 완료 알림 SMS를 전송합니다.
     *
     * @param to          수신자 번호
     * @param productName 등록된 상품 이름
     * @param productUrl  등록된 상품 URL
     */
    public void sendRegistrationAlert(String to, String productName, String productUrl) {
        String messageContent = String.format(
                "관심 상품 등록 완료\n상품: %s\n상품 URL: %s",
                productName, productUrl);
        sendSms(to, messageContent);
    }

    /**
     * 재고 상태 변경 알림 SMS를 전송합니다.
     *
     * @param to          수신자 번호
     * @param productName 변경된 상품 이름
     * @param productUrl  변경된 상품 URL
     * @param stockStatus 현재 재고 상태 (예: '재고 있음', '재고 없음')
     */
    public void sendStockChangeAlert(String to, String productName, String productUrl, String stockStatus) {
        String messageContent = String.format(
                "재고 상태 변경 알림\n상품: %s\n상품 URL: %s\n현재 재고 상태: %s",
                productName, productUrl, stockStatus);
        sendSms(to, messageContent);
    }

    /**
     * 알림 유형에 따라 SMS 메시지를 생성하고 전송합니다.
     *
     * @param notificationType 알림 유형 ("stockChange" 또는 "registration")
     * @param to               수신자 번호
     * @param productName      상품 이름
     * @param productUrl       상품 URL
     * @param stockStatus      재고 상태 (stockChange인 경우 사용, registration이면 빈 문자열)
     */
    public void sendSmsNotification(String notificationType, String to, String productName, String productUrl, String stockStatus) {
        if ("stockChange".equalsIgnoreCase(notificationType)) {
            sendStockChangeAlert(to, productName, productUrl, stockStatus);
        } else {
            sendRegistrationAlert(to, productName, productUrl);
        }
    }
}
