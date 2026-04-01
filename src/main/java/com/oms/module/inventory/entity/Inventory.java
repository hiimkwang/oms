package com.oms.module.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", uniqueConstraints = {
        // Đảm bảo 1 biến thể tại 1 chi nhánh chỉ có 1 bản ghi tồn kho
        @UniqueConstraint(columnNames = {"variant_id", "branch_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    // Tồn kho vật lý (Số lượng hàng thực tế đang nằm trong kho)
    @Column(name = "stock", nullable = false)
    private Integer stock;

    // Tồn kho khả dụng (Số lượng có thể bán = Tồn vật lý - Hàng khách đã đặt nhưng chưa giao)
    @Column(name = "available_stock", nullable = false)
    private Integer availableStock;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}