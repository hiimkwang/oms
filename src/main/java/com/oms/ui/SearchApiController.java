package com.oms.ui;

import com.oms.module.customer.repository.CustomerRepository;
import com.oms.module.order.repository.OrderRepository;
import com.oms.module.product.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchApiController {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public SearchApiController(ProductRepository productRepository, CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> searchOverview(@RequestParam("keyword") String keyword) {
        Map<String, Object> results = new HashMap<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(results);
        }

        var products = productRepository.searchAndFilterProducts(keyword, null, null)
                .stream().limit(5)
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("name", p.getName());
                    map.put("code", p.getSku() != null ? p.getSku() : "");
                    map.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
                    return map;
                })
                .collect(Collectors.toList());

        var customers = customerRepository.searchByKeyword(keyword)
                .stream().limit(5)
                .map(c -> Map.of(
                        "id", c.getId(),
                        "name", c.getFullName(),
                        "phone", c.getPhone() != null ? c.getPhone() : "",
                        "code", c.getCode() != null ? c.getCode() : "" // THÊM DÒNG NÀY ĐỂ LẤY MÃ KHÁCH HÀNG
                ))
                .collect(Collectors.toList());

        var orders = orderRepository.searchByKeyword(keyword)
                .stream().limit(5)
                .map(o -> Map.of("id", o.getId(), "code", o.getOrderCode()))
                .collect(Collectors.toList());

        results.put("products", products);
        results.put("customers", customers);
        results.put("orders", orders);

        return ResponseEntity.ok(results);
    }
}