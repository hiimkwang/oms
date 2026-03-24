package com.oms.module.setting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "master_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_type", nullable = false)
    private String dataType; // Ví dụ: "BRAND", "CATEGORY", "UNIT", "WARRANTY", "CONDITION"

    @Column(name = "data_value", nullable = false)
    private String dataValue; // Ví dụ: "Aula", "Bàn phím cơ", "Chiếc", "6 Tháng"

    @Column(name = "sort_order")
    private Integer sortOrder; // Để sắp xếp thứ tự hiển thị trên Dropdown
}