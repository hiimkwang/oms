package com.oms.module.customer.repository;

import com.oms.module.customer.dto.CustomerRequest;
import com.oms.module.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT new com.oms.module.customer.dto.CustomerRequest(" +
            "c.code, c.fullName, c.phone, c.email, c.company, c.address, c.taxCode, c.customerGroup, c.note, " +
            "COUNT(o.id), " +                // Trả về Long (Tham số 10)
            "MAX(o.createdAt), " +           // Trả về LocalDateTime (Tham số 11)
            "SUM(COALESCE(o.totalAmount, 0.0))) " + // Dùng 0.0 để ép ra Double (Tham số 12)
            "FROM Customer c " +
            "LEFT JOIN Order o ON o.customer = c " + // JOIN trực tiếp qua quan hệ @ManyToOne
            "WHERE (:kw IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) OR c.phone LIKE CONCAT('%', :kw, '%')) " +
            "GROUP BY c.id, c.code, c.fullName, c.phone, c.email, c.company, c.address, c.taxCode, c.customerGroup, c.note")
    List<CustomerRequest> findAllWithStats(@Param("kw") String keyword);

    boolean existsByPhone(String phone);
}