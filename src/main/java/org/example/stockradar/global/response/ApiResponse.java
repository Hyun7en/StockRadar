package org.example.stockradar.global.response;

import org.example.stockradar.global.exception.ErrorCode;

/**
 * JSON API 공통 응답 형식.
 * 성공: { success: true, data, message }
 * 실패: { success: false, message, errorCode }
 */
public record ApiResponse<T>(boolean success, T data, String message, String errorCode) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return error(errorCode, errorCode.getDescription());
    }

    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode.getErrorCode());
    }
}
