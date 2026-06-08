package com.oms.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Lỗi phân quyền (@PreAuthorize) -> 403, không lộ chi tiết
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Map<String, String>> handleAccessDenied(Exception ex) {
        log.warn("Truy cập bị từ chối: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Bạn không có quyền thực hiện thao tác này."));
    }

    // Lỗi validation @Valid -> 400 kèm thông báo theo từng field (an toàn)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));
        Map<String, String> response = new HashMap<>();
        response.put("message", "Dữ liệu không hợp lệ.");
        response.putAll(errors);
        return ResponseEntity.badRequest().body(response);
    }

    // Vi phạm ràng buộc DB (unique, FK...) -> 400 generic, KHÔNG lộ câu SQL/schema
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Vi phạm ràng buộc dữ liệu: ", ex);
        return ResponseEntity.badRequest()
                .body(Map.of("message", "Dữ liệu không hợp lệ hoặc đã tồn tại trong hệ thống."));
    }

    // Các lỗi lập trình thường gặp -> 500 generic, KHÔNG lộ stacktrace/message nội bộ
    @ExceptionHandler({NullPointerException.class, IllegalStateException.class, ClassCastException.class})
    public ResponseEntity<Map<String, String>> handleProgrammingError(RuntimeException ex) {
        log.error("Lỗi hệ thống: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau!"));
    }

    // Lỗi nghiệp vụ chủ động ném ra (thông báo tiếng Việt thân thiện) -> 400
    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBusiness(RuntimeException ex) {
        log.warn("Business Exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Yêu cầu không hợp lệ."));
    }

    // RuntimeException còn lại: vẫn giữ message (đa số là message nghiệp vụ tiếng Việt do code chủ động ném)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime Exception: {}", ex.getMessage());
        String msg = ex.getMessage();
        // Phòng trường hợp message rỗng/null
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", (msg != null && !msg.isBlank()) ? msg : "Yêu cầu không hợp lệ."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("System Error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau!"));
    }
}
