package com.oms.config.exception;

/**
 * Exception nghiệp vụ chủ động: thông báo trong message là an toàn để hiển thị cho người dùng cuối.
 * Dùng exception này (thay cho RuntimeException trần) khi muốn báo lỗi nghiệp vụ rõ ràng.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
