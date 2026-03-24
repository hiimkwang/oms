package com.oms.module.report.controller;

import com.oms.module.report.dto.ProfitReportResponse;
import com.oms.module.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Ví dụ gọi API: GET /api/v1/reports/profit?month=3&year=2026
    @GetMapping("/profit")
    public ResponseEntity<ProfitReportResponse> getProfitReport(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(reportService.getMonthlyProfitReport(month, year));
    }
}