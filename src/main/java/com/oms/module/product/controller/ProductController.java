package com.oms.module.product.controller;

import com.oms.module.product.dto.ProductRequest;
import com.oms.module.product.entity.Product;
import com.oms.module.product.entity.ProductVariant;
import com.oms.module.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {


    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{sku}")
    public ResponseEntity<Product> updateProduct(@PathVariable String sku, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(sku, request));
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String sku) {
        productService.deleteProduct(sku);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/variants/search")
    public ResponseEntity<List<ProductVariant>> searchVariants(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.searchVariantsForOrder(keyword));
    }
}