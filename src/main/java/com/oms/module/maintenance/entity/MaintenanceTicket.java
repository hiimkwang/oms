package com.oms.module.maintenance.entity;

import com.oms.module.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "maintenance_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", unique = true, nullable = false)
    private String ticketCode; // Số phiếu nhận (VD: BH000001)

    @Column(name = "receive_date", nullable = false)
    private LocalDate receiveDate; // Ngày nhận

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer; // Khách hàng mang đến sửa/bảo hành

    @Column(name = "product_name", nullable = false)
    private String productName; // Tên thiết bị (có thể nhập tay vì đôi khi là phím khách mua chỗ khác mang tới sửa)

    @Column(name = "serial_number")
    private String serialNumber; // Số Serial

    @Column(name = "reported_defect", columnDefinition = "TEXT")
    private String reportedDefect; // Lỗi khách báo

    @Column(name = "actual_condition", columnDefinition = "TEXT")
    private String actualCondition; // Hiện trạng thực tế khi kiểm tra

    @Column(name = "estimated_cost")
    private Double estimatedCost; // Báo giá dự kiến

    @Column(name = "customer_agreed")
    private Boolean customerAgreed; // Khách chốt sửa (True/False)

    @Column(name = "technician")
    private String technician; // Kỹ thuật phụ trách

    @Column(name = "processing_details", columnDefinition = "TEXT")
    private String processingDetails; // Nội dung xử lý

    @Column(name = "actual_cost")
    private Double actualCost; // Chi phí thực tế

    @Column(name = "return_date")
    private LocalDate returnDate; // Ngày trả khách

    @Column(name = "status")
    private String status; // Trạng thái (VD: Đang kiểm tra, Đang sửa, Chờ trả khách, Đã xong)

    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú

    @PrePersist
    protected void onCreate() {
        if (this.receiveDate == null) {
            this.receiveDate = LocalDate.now();
        }
    }
}