package com.oms.module.warranty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "warranty_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ticketCode; // Mã phiếu: BH-xxxxxx

    private String customerName;
    private String customerPhone;

    private String productName;
    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    private String issueDescription;

    private LocalDateTime receiveDate;
    private LocalDateTime returnDate;

    private BigDecimal repairCost;

    private Long branchId; // Lưu chi nhánh nhận máy

    @Enumerated(EnumType.STRING)
    private TicketType type;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC") // Tự động sắp xếp mới nhất lên đầu
    private List<WarrantyActivity> activities;

    public enum TicketType {
        WARRANTY, // Bảo hành
        REPAIR    // Sửa chữa dịch vụ
    }

    public enum TicketStatus {
        RECEIVED,   // Mới tiếp nhận
        PROCESSING, // Đang xử lý
        DONE,       // Đã sửa xong
        RETURNED,   // Đã trả khách
        CANCELED    // Đã hủy
    }
}