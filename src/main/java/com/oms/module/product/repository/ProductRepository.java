package com.oms.module.product.repository;

import com.oms.module.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    // Đã sửa lại logic lọc Category theo ID
    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:brand IS NULL OR :brand = '' OR p.brand = :brand) " +
            "ORDER BY p.createdAt DESC")
    List<Product> searchAndFilterProducts(@Param("keyword") String keyword,
                                          @Param("categoryId") Long categoryId, // Sửa thành Long categoryId
                                          @Param("brand") String brand);

    @Modifying
    @Query("UPDATE Product p SET p.category = null WHERE p.category.id = :categoryId")
    void setCategoryNullByCategoryId(@Param("categoryId") Long categoryId);

}