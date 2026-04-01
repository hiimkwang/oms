package com.oms.module.customer.repository;

import com.oms.module.common.dto.TargetDropdownResponse;
import com.oms.module.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByPhone(String phone);

    Optional<Customer> findByCode(String code);

    // Lọc theo keyword (mã, tên, SĐT)
    @Query("SELECT c FROM Customer c WHERE :keyword IS NULL OR :keyword = '' " +
            "OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR c.phone LIKE CONCAT('%', :keyword, '%') ORDER BY c.createdAt DESC")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);
    @Modifying
    @Query("DELETE FROM Customer c WHERE c.code IN :codes")
    void deleteAllByCodeIn(@Param("codes") List<String> codes);
    long countByCustomerGroup(String customerGroup);
    List<Customer> findByFullNameContainingIgnoreCaseOrPhoneContaining(String fullName, String phone);
}
