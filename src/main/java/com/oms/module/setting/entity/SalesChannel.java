package com.oms.module.setting.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sales_channels")
@Data
public class SalesChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // VD: Shopee, Lazada, Facebook, Zalo
    private String code;
    private String description;
    private boolean active = true;
}