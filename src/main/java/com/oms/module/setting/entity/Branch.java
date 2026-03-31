package com.oms.module.setting.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "branches")
@Data
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;
    private String phone;
    private boolean active = true;
}