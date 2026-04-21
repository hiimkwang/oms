package com.oms.module.tool.controller;

import com.oms.module.tool.service.PricingDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/inventory/pricing-draft")
@RequiredArgsConstructor
public class PricingDraftApiController {

    private final PricingDraftService pricingDraftService;

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    @GetMapping
    public ResponseEntity<?> getDraft() {
        return pricingDraftService.getDraftByUsername(getCurrentUsername())
                .map(draft -> ResponseEntity.ok(draft.getDraftData()))
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<?> saveDraft(@RequestBody String draftJson) {
        pricingDraftService.saveDraft(getCurrentUsername(), draftJson);
        return ResponseEntity.ok().body("{\"status\": \"success\"}");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteDraft() {
        pricingDraftService.deleteDraft(getCurrentUsername());
        return ResponseEntity.ok().body("{\"status\": \"success\"}");
    }
}