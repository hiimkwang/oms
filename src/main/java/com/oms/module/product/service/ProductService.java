package com.oms.module.product.service;

import com.oms.module.product.dto.ProductRequest;
import com.oms.module.product.dto.ProductVariantRequest;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductRepository;
import com.oms.module.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    // ================= 1. TẠO MỚI SẢN PHẨM =================
    @Transactional
    public Product createProduct(ProductRequest request) {
        // Tự sinh SKU mẹ nếu rỗng
        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty() || "AUTO".equals(sku)) {
            sku = "MK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        Product product = Product.builder()
                .sku(sku)
                .name(request.getName())
                .category(request.getCategory())
                .brand(request.getBrand())
                .conditionStatus(request.getConditionStatus())
                .unit(request.getUnit())
                .minStockLevel(request.getMinStockLevel())
                .price(request.getPrice())
                .description(request.getDescription())
                .warrantyPeriod(request.getWarrantyPeriod())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .build();

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ProductVariant> variantList = new ArrayList<>();
            int totalStock = 0;

            for (ProductVariantRequest vReq : request.getVariants()) {
                String varSku = vReq.getSku();
                if (varSku == null || varSku.trim().isEmpty() || varSku.startsWith("AUTO")) {
                    varSku = sku + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                }

                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .variantName(vReq.getVariantName())
                        .sku(varSku)
                        .price(vReq.getPrice() != null ? vReq.getPrice() : request.getPrice())
                        .costPrice(vReq.getCostPrice() != null ? vReq.getCostPrice() : BigDecimal.ZERO)
                        .stockQuantity(vReq.getStockQuantity() != null ? vReq.getStockQuantity() : 0)
                        .build();

                totalStock += variant.getStockQuantity();
                variantList.add(variant);
            }

            product.setVariants(variantList);
            product.setStockQuantity(totalStock);
        }

        return productRepository.save(product);
    }

    // ================= 2. LẤY DANH SÁCH TẤT CẢ SẢN PHẨM =================
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        // Lấy tất cả và sắp xếp mới nhất lên đầu (Nếu rảnh ông thêm sort trong Repo cũng được)
        return productRepository.findAll();
    }

    // ================= 3. LẤY CHI TIẾT THEO SKU =================
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với SKU: " + sku));
    }

    // ================= 4. CẬP NHẬT SẢN PHẨM THEO SKU =================
    @Transactional
    public Product updateProduct(String sku, ProductRequest request) {
        Product existingProduct = getProductBySku(sku); // Tìm theo SKU thay vì ID

        // Cập nhật thông tin cơ bản
        existingProduct.setName(request.getName());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setConditionStatus(request.getConditionStatus());
        existingProduct.setUnit(request.getUnit());
        existingProduct.setMinStockLevel(request.getMinStockLevel());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setWarrantyPeriod(request.getWarrantyPeriod());

        // Cập nhật Biến thể (Variants)
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            Map<String, ProductVariant> existingVariantsMap = existingProduct.getVariants().stream()
                    .collect(Collectors.toMap(ProductVariant::getSku, v -> v));

            List<ProductVariant> updatedVariants = new ArrayList<>();
            int totalStock = 0;

            for (ProductVariantRequest vReq : request.getVariants()) {
                String varSku = vReq.getSku();
                if (varSku == null || varSku.trim().isEmpty() || varSku.startsWith("AUTO")) {
                    varSku = existingProduct.getSku() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                }

                ProductVariant variant;
                if (existingVariantsMap.containsKey(varSku)) {
                    variant = existingVariantsMap.get(varSku);
                    variant.setVariantName(vReq.getVariantName());
                    variant.setPrice(vReq.getPrice() != null ? vReq.getPrice() : request.getPrice());
                    variant.setCostPrice(vReq.getCostPrice() != null ? vReq.getCostPrice() : BigDecimal.ZERO);
                    variant.setStockQuantity(vReq.getStockQuantity() != null ? vReq.getStockQuantity() : 0);
                } else {
                    variant = ProductVariant.builder()
                            .product(existingProduct)
                            .variantName(vReq.getVariantName())
                            .sku(varSku)
                            .price(vReq.getPrice() != null ? vReq.getPrice() : request.getPrice())
                            .costPrice(vReq.getCostPrice() != null ? vReq.getCostPrice() : BigDecimal.ZERO)
                            .stockQuantity(vReq.getStockQuantity() != null ? vReq.getStockQuantity() : 0)
                            .build();
                }
                totalStock += variant.getStockQuantity();
                updatedVariants.add(variant);
            }

            existingProduct.getVariants().clear();
            existingProduct.getVariants().addAll(updatedVariants);
            existingProduct.setStockQuantity(totalStock);

        } else {
            existingProduct.getVariants().clear();
            existingProduct.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        }

        return productRepository.save(existingProduct);
    }

    // ================= 5. XÓA SẢN PHẨM THEO SKU =================
    @Transactional
    public void deleteProduct(String sku) {
        Product product = getProductBySku(sku); // Tìm SP trước rồi mới xóa
        productRepository.delete(product);
    }
}