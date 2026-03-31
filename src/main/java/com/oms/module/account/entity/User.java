package com.oms.module.account.entity;

import com.oms.module.setting.entity.Branch;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role; // Enum: ADMIN, STAFF

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch; // Nhân viên thuộc chi nhánh nào

    private boolean active = true;

    public enum Role {
        ADMIN, STAFF
    }
}