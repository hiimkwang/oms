package com.oms.module.supplier.entity;

import com.oms.module.supplier.enums.SupplierStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String code; // Mã nhà cung cấp (Ví dụ: NCC0001)

    @Column(nullable = false)
    private String name; // Tên nhà cung cấp (Bắt buộc)

    // Liên hệ & Thông tin chung
    private String phone;
    private String email;
    private String taxCode; // Mã số thuế
    private String website;
    private String fax;

    // Địa chỉ
    private String country;
    private String province;      // Tỉnh/Thành phố
    private String district;      // Quận/Huyện
    private String ward;          // Phường/Xã
    private String addressDetail; // Địa chỉ cụ thể

    // Phân loại
    private String assignee; // Nhân viên phụ trách (Có thể lưu username hoặc ID nhân viên)

    @Column(length = 500)
    private String tags; // Lưu các tag cách nhau bằng dấu phẩy, ví dụ: "VIP, Đồ điện tử"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierStatus status = SupplierStatus.ACTIVE; // Mặc định là Đang hoạt động

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Tự động sinh mã NCC nếu lúc thêm mới người dùng bỏ trống
    @PrePersist
    public void generateCodeIfEmpty() {
        if (this.code == null || this.code.trim().isEmpty()) {
            this.code = "NCC" + System.currentTimeMillis(); // Cách sinh mã tạm, ông có thể dùng logic sequence sau
        }
    }
}