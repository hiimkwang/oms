package com.oms.module.supplier.repository;

import com.oms.module.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT s FROM Supplier s WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "s.phone LIKE CONCAT('%', :keyword, '%')) " +
            "ORDER BY s.createdAt DESC")
    List<Supplier> searchSuppliers(@Param("keyword") String keyword);
}