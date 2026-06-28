package com.oms.module.setting.controller;

import com.oms.module.setting.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupApiController {

    private final BackupService backupService;

    // Sao lưu ngay
    @PostMapping("/run")
    public ResponseEntity<?> runNow() {
        BackupService.BackupResult result = backupService.performBackup();
        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fileName", result.fileName(),
                    "sizeKb", result.sizeKb(),
                    "message", result.message()));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", result.message()));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(backupService.listBackups());
    }
}
