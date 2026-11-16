package org.example.stockradar.global.exception;

import lombok.Getter;

/**
 * 애플리케이션에서 의도적으로 던지는 단일 예외 타입.
 * HTTP 상태와 에러 코드는 {@link ErrorCode}가 결정한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    /** 기본 설명 대신 상황에 맞는 메시지를 내려줄 때 사용 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
