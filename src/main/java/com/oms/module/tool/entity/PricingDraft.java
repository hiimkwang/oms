package com.oms.module.tool.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_drafts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true)
    private String username; // Lưu nháp theo từng tài khoản

    @Column(name = "draft_data", columnDefinition = "LONGTEXT")
    private String draftData; // Lưu nguyên chuỗi JSON

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}