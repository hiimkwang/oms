package com.oms.module.setting.controller;

import com.oms.module.setting.entity.SalesChannel;
import com.oms.module.setting.repository.SalesChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings/channels")
@RequiredArgsConstructor
public class SalesChannelApiController {

    private final SalesChannelRepository channelRepository;

    // API Tạo mới kênh
    @PostMapping
    public ResponseEntity<SalesChannel> createChannel(@RequestBody SalesChannel request) {
        request.setActive(true); // Mặc định kênh mới là đang hoạt động
        request.setCode(request.getCode().toUpperCase()); // Đảm bảo code luôn in hoa
        SalesChannel saved = channelRepository.save(request);
        return ResponseEntity.ok(saved);
    }

    // API Cập nhật kênh
    @PutMapping("/{id}")
    public ResponseEntity<SalesChannel> updateChannel(@PathVariable Long id, @RequestBody SalesChannel request) {
        SalesChannel channel = channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kênh bán hàng"));

        channel.setName(request.getName());
        channel.setCode(request.getCode().toUpperCase());
        channel.setActive(request.isActive());

        SalesChannel updated = channelRepository.save(channel);
        return ResponseEntity.ok(updated);
    }
}