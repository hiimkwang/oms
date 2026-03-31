package com.oms.module.setting.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSetting {
    @Id
    private String settingKey; // VD: STORE_NAME, TAX_RATE, CURRENCY
    private String settingValue;
    private String description;
}