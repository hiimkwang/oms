package com.oms.module.product.service;

import com.oms.module.category.entity.Category;
import com.oms.module.category.repository.CategoryRepository;
import com.oms.module.product.dto.ProductRequest;
import com.oms.module.product.dto.ProductVariantRequest;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.repository.ProductRepository;
import com.oms.module.product.repository.ProductVariantRepository;
// Import thêm Inventory
import com.oms.module.inventory.entity.Inventory;
import com.oms.module.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    // Bổ sung repo quản lý kho
    private final InventoryRepository inventoryRepository;

    // ================= 1. TẠO MỚI SẢN PHẨM (ĐÃ CHUẨN HÓA KHO BÃI) =================
    @Transactional
    public Product createProduct(ProductRequest request) {
        // 1. Tự sinh SKU mẹ nếu rỗng
        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty() || "AUTO".equals(sku)) {
            sku = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục này trong hệ thống!"));
        }

        // 2. Tạo Product (Sản phẩm gốc)
        Boolean isManageStock = request.getManageStock() != null ? request.getManageStock() : true;
        Product product = Product.builder()
                .sku(sku)
                .name(request.getName())
                .category(category)
                .brand(request.getBrand())
                .conditionStatus(request.getConditionStatus())
                .unit(request.getUnit())
                .minStockLevel(request.getMinStockLevel())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .warrantyPeriod(request.getWarrantyPeriod())
                .manageStock(isManageStock) // Bổ sung cờ quản lý kho
                .stockQuantity(0) // Tạm gán 0, sẽ cộng dồn từ biến thể lên
                .build();

        // Lưu Product mẹ trước để lấy ID gán cho các Variant con
        product = productRepository.save(product);

        List<ProductVariant> variantList = new ArrayList<>();
        int totalStock = 0;

        // 3. Xử lý Biến thể và Lưu Kho
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            // TRƯỜNG HỢP A: CÓ NHIỀU BIẾN THỂ
            for (ProductVariantRequest vReq : request.getVariants()) {
                String varSku = vReq.getSku();
                if (varSku == null || varSku.trim().isEmpty() || varSku.startsWith("AUTO")) {
                    varSku = sku + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                }

                int vStock = vReq.getStockQuantity() != null ? vReq.getStockQuantity() : 0;

                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .variantName(vReq.getVariantName())
                        .sku(varSku)
                        .imageUrl(vReq.getImageUrl())
                        .price(vReq.getPrice() != null ? vReq.getPrice() : request.getPrice())
                        .costPrice(vReq.getCostPrice() != null ? vReq.getCostPrice() : BigDecimal.ZERO)
                        .stockQuantity(vStock)
                        .build();

                variant = productVariantRepository.save(variant);
                variantList.add(variant);
                totalStock += vStock;

                // LƯU KHO CHO TỪNG BIẾN THỂ
                if (isManageStock && request.getBranchId() != null && vStock > 0) {
                    createInitialInventory(variant.getId(), request.getBranchId(), vStock);
                }
            }
        } else {
            // TRƯỜNG HỢP B: SẢN PHẨM ĐƠN (KHÔNG CÓ BIẾN THỂ)
            // Tự sinh 1 biến thể "Mặc định" để hệ thống Kho vận hành đồng nhất
            int vStock = request.getStockQuantity() != null ? request.getStockQuantity() : 0;

            ProductVariant defaultVariant = ProductVariant.builder()
                    .product(product)
                    .variantName("Mặc định")
                    .sku(sku) // Lấy luôn SKU mẹ cho gọn
                    .imageUrl(request.getImageUrl())
                    .price(request.getPrice())
                    .costPrice(BigDecimal.ZERO)
                    .stockQuantity(vStock)
                    .build();

            defaultVariant = productVariantRepository.save(defaultVariant);
            variantList.add(defaultVariant);
            totalStock += vStock;

            // LƯU KHO CHO SẢN PHẨM ĐƠN
            if (isManageStock && request.getBranchId() != null && vStock > 0) {
                createInitialInventory(defaultVariant.getId(), request.getBranchId(), vStock);
            }
        }

        // Cập nhật lại list biến thể và tổng tồn kho cho SP mẹ
        product.setVariants(variantList);
        product.setStockQuantity(totalStock);
        return productRepository.save(product);
    }

    // Hàm phụ trợ lưu dữ liệu vào bảng Inventory (Kho)
    private void createInitialInventory(Long variantId, Long branchId, Integer stock) {
        Inventory inventory = new Inventory();
        inventory.setVariantId(variantId);
        inventory.setBranchId(branchId);
        inventory.setStock(stock);
        inventory.setAvailableStock(stock); // Tồn khả dụng ban đầu bằng tồn thực tế
        inventoryRepository.save(inventory);
    }

    // ================= 2. LẤY DANH SÁCH TẤT CẢ SẢN PHẨM =================
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ================= 3. LẤY CHI TIẾT THEO SKU =================
    public Product getProductBySku(String sku) {
        Optional<Product> productOpt = productRepository.findBySku(sku);
        if (productOpt.isPresent()) {
            return productOpt.get();
        }

        ProductVariant variant = productVariantRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm hoặc biến thể với SKU: " + sku));

        return variant.getProduct();
    }

    // ================= 4. CẬP NHẬT SẢN PHẨM THEO SKU =================
    @Transactional
    public Product updateProduct(String sku, ProductRequest request) {
        Product existingProduct = getProductBySku(sku);
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
            existingProduct.setCategory(category);
        }

        existingProduct.setName(request.getName());
        existingProduct.setCategory(category);
        existingProduct.setBrand(request.getBrand());
        existingProduct.setConditionStatus(request.getConditionStatus());
        existingProduct.setUnit(request.getUnit());
        existingProduct.setMinStockLevel(request.getMinStockLevel());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setImageUrl(request.getImageUrl());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setWarrantyPeriod(request.getWarrantyPeriod());

        // Update cờ quản lý kho nếu có
        if (request.getManageStock() != null) {
            existingProduct.setManageStock(request.getManageStock());
        }

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
                    variant.setImageUrl(vReq.getImageUrl());
                    variant.setPrice(vReq.getPrice() != null ? vReq.getPrice() : request.getPrice());
                    variant.setCostPrice(vReq.getCostPrice() != null ? vReq.getCostPrice() : BigDecimal.ZERO);
                    // Thông thường khi update SP, ta không update tồn kho ở đây mà phải qua phiếu kiểm kho.
                    // Tạm giữ logic cũ của anh
                    variant.setStockQuantity(vReq.getStockQuantity() != null ? vReq.getStockQuantity() : variant.getStockQuantity());
                } else {
                    variant = ProductVariant.builder()
                            .product(existingProduct)
                            .variantName(vReq.getVariantName())
                            .sku(varSku)
                            .imageUrl(vReq.getImageUrl())
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
            // Tạm thời giữ nguyên logic nếu update SP Đơn (Mặc định)
            existingProduct.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : existingProduct.getStockQuantity());
        }

        return productRepository.save(existingProduct);
    }

    // ================= 5. XÓA SẢN PHẨM =================
    @Transactional
    public void deleteProduct(String sku) {
        Product product = getProductBySku(sku);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getFilteredProducts(String keyword, Long category, String brand) {
        return productRepository.searchAndFilterProducts(keyword, category, brand);
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> getFilteredInventory(String keyword, String stockStatus, Integer minStock, Integer maxStock, String dateRange) {
        java.time.LocalDateTime startDate = null;
        java.time.LocalDateTime endDate = null;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        if (dateRange != null && !dateRange.isEmpty()) {
            switch (dateRange) {
                case "today":
                    startDate = now.with(java.time.LocalTime.MIN);
                    endDate = now.with(java.time.LocalTime.MAX);
                    break;
                case "yesterday":
                    startDate = now.minusDays(1).with(java.time.LocalTime.MIN);
                    endDate = now.minusDays(1).with(java.time.LocalTime.MAX);
                    break;
                case "7days":
                    startDate = now.minusDays(7).with(java.time.LocalTime.MIN);
                    endDate = now.with(java.time.LocalTime.MAX);
                    break;
                case "this_month":
                    startDate = now.withDayOfMonth(1).with(java.time.LocalTime.MIN);
                    endDate = now.with(java.time.LocalTime.MAX);
                    break;
                case "last_month":
                    startDate = now.minusMonths(1).withDayOfMonth(1).with(java.time.LocalTime.MIN);
                    endDate = now.withDayOfMonth(1).minusDays(1).with(java.time.LocalTime.MAX);
                    break;
                case "this_year":
                    startDate = now.withDayOfYear(1).with(java.time.LocalTime.MIN);
                    endDate = now.with(java.time.LocalTime.MAX);
                    break;
            }
        }
        return productVariantRepository.searchAndFilterVariants(keyword, stockStatus, minStock, maxStock, startDate, endDate);
    }

    public List<ProductVariant> searchVariantsForOrder(String keyword) {
        return productVariantRepository.searchAndFilterVariants(
                keyword, null, null, null, null, null
        );
    }

    @Transactional
    public void deleteProductsBulk(List<Long> ids) {
        productVariantRepository.deleteByProductIdIn(ids);
        productRepository.deleteAllByIdInBatch(ids);
    }
}