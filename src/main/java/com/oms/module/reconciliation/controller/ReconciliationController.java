package com.oms.module.reconciliation.controller;

import com.oms.module.reconciliation.dto.ReconcileResult;
import com.oms.module.reconciliation.dto.ReconcileSyncRequest;
import com.oms.module.reconciliation.dto.ReconcileSyncResult;
import com.oms.module.reconciliation.service.ReconciliationService;
import com.oms.module.setting.service.SalesChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final SalesChannelService salesChannelService;

    @GetMapping("/ui/reconciliation")
    public String page(Model model) {
        model.addAttribute("channels", salesChannelService.findAll());
        return "reconciliation/reconcile";
    }

    @PostMapping("/api/v1/reconciliation/upload")
    @ResponseBody
    public ResponseEntity<ReconcileResult> upload(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(required = false) String channel,
                                                  @RequestParam(required = false) String fromDate,
                                                  @RequestParam(required = false) String toDate) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(ReconcileResult.builder()
                    .success(false).message("Vui lòng chọn file đối soát.").build());
        }

        LocalDateTime start = parseStart(fromDate);
        LocalDateTime end = parseEnd(toDate);
        String ch = (channel != null && !channel.isBlank() && !"ALL".equalsIgnoreCase(channel)) ? channel : null;

        ReconcileResult result = reconciliationService.reconcile(file, ch, start, end);
        return ResponseEntity.ok(result);
    }

    // Đồng bộ kết quả đối soát về OMS (tạo đơn bù / điền mã / đánh dấu thanh toán)
    @PostMapping("/api/v1/reconciliation/sync")
    @ResponseBody
    public ResponseEntity<ReconcileSyncResult> sync(@RequestBody ReconcileSyncRequest request) {
        return ResponseEntity.ok(reconciliationService.sync(request));
    }

    private LocalDateTime parseStart(String d) {
        if (d == null || d.isBlank()) return LocalDateTime.now().minusDays(60).with(LocalTime.MIN);
        return LocalDate.parse(d).atTime(LocalTime.MIN);
    }

    private LocalDateTime parseEnd(String d) {
        if (d == null || d.isBlank()) return LocalDateTime.now().with(LocalTime.MAX);
        return LocalDate.parse(d).atTime(LocalTime.MAX);
    }
}
