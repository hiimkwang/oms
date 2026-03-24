package com.oms.module.product.service;


import com.oms.module.product.dto.ProductRequest;
import com.oms.module.product.entity.Product;
import com.oms.module.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với mã SKU: " + sku));
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new RuntimeException("Mã SKU đã tồn tại trong hệ thống: " + request.getSku());
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .category(request.getCategory())
                .brand(request.getBrand())
                .conditionStatus(request.getConditionStatus())
                .unit(request.getUnit())
                .minStockLevel(request.getMinStockLevel())
                .retailPrice(request.getRetailPrice())
                .warrantyPeriod(request.getWarrantyPeriod())
                .note(request.getNote())
                // Tồn kho và Giá vốn khởi tạo bằng 0
                .stockQuantity(0)
                .build();

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(String sku, ProductRequest request) {
        Product product = getProductBySku(sku);

        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setBrand(request.getBrand());
        product.setConditionStatus(request.getConditionStatus());
        product.setUnit(request.getUnit());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setRetailPrice(request.getRetailPrice());
        product.setWarrantyPeriod(request.getWarrantyPeriod());
        product.setNote(request.getNote());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(String sku) {
        Product product = getProductBySku(sku);
        // Sau này sẽ cần check xem SP đã phát sinh giao dịch nhập/xuất chưa trước khi xóa cứng
        productRepository.delete(product);
    }
}