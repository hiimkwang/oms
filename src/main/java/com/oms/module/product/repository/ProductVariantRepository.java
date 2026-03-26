package com.oms.module.product.repository;

import com.oms.module.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    @Query("SELECT v FROM ProductVariant v WHERE " +
            "(:keyword IS NULL OR :keyword = '' " +
            "  OR LOWER(v.variantName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "  OR LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "  OR LOWER(v.product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:stockStatus IS NULL OR :stockStatus = '' OR (:stockStatus = 'in_stock' AND v.stockQuantity > 0) OR (:stockStatus = 'out_of_stock' AND v.stockQuantity <= 0)) AND " +
            "(:minStock IS NULL OR v.stockQuantity >= :minStock) AND " +
            "(:maxStock IS NULL OR v.stockQuantity <= :maxStock) AND " +
            "(cast(:startDate as timestamp) IS NULL OR v.product.createdAt >= :startDate) AND " +
            "(cast(:endDate as timestamp) IS NULL OR v.product.createdAt <= :endDate) " +
            "ORDER BY v.product.createdAt DESC")
    List<ProductVariant> searchAndFilterVariants(
            @Param("keyword") String keyword,
            @Param("stockStatus") String stockStatus,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    Optional<ProductVariant> findBySku(String sku);
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductVariant v WHERE v.product.id IN :productIds")
    void deleteByProductIdIn(@Param("productIds") List<Long> productIds);
}