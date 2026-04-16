package com.oms.module.inventory.controller;

import com.oms.module.inventory.entity.PricingDraft;
import com.oms.module.inventory.repository.PricingDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/inventory/pricing-draft")
@RequiredArgsConstructor
public class PricingDraftApiController {

    private final PricingDraftRepository draftRepository;

    // Lấy tên user đang đăng nhập
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    // 1. LẤY BẢN NHÁP
    @GetMapping
    public ResponseEntity<?> getDraft() {
        return draftRepository.findByUsername(getCurrentUsername())
                .map(draft -> ResponseEntity.ok(draft.getDraftData()))
                .orElse(ResponseEntity.noContent().build()); // Trả về 204 nếu chưa có nháp
    }

    // 2. LƯU BẢN NHÁP (Nhận chuỗi JSON từ Frontend)
    @PostMapping
    public ResponseEntity<?> saveDraft(@RequestBody String draftJson) {
        String username = getCurrentUsername();
        PricingDraft draft = draftRepository.findByUsername(username).orElse(new PricingDraft());

        draft.setUsername(username);
        draft.setDraftData(draftJson);
        draft.setUpdatedAt(LocalDateTime.now());

        draftRepository.save(draft);
        return ResponseEntity.ok().body("{\"status\": \"success\"}");
    }

    // 3. XÓA BẢN NHÁP (Khi nhấn nút Làm mới)
    @DeleteMapping
    public ResponseEntity<?> deleteDraft() {
        draftRepository.findByUsername(getCurrentUsername()).ifPresent(draftRepository::delete);
        return ResponseEntity.ok().body("{\"status\": \"success\"}");
    }
}