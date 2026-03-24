package com.oms.ui;

import com.oms.module.customer.service.CustomerService;
import com.oms.module.product.service.ProductService;
import com.oms.module.report.dto.ProfitReportResponse;
import com.oms.module.report.service.ReportService;
import com.oms.module.setting.service.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductService productService;
    private final ReportService reportService; // Thêm ReportService
    private final CustomerService customerService;
    private  final MasterDataService masterDataService;
    // Trang chủ - Bảng điều khiển (Dashboard)
    @GetMapping("/")
    public String dashboard(Model model) {
        // Lấy tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Gọi hàm tính toán Lợi nhuận ròng tự động
        ProfitReportResponse report = reportService.getMonthlyProfitReport(currentMonth, currentYear);

        // Truyền dữ liệu sang giao diện
        model.addAttribute("report", report);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentYear);

        // Đếm số lượng sản phẩm trong kho
        int totalProducts = productService.getAllProducts().size();
        model.addAttribute("totalProducts", totalProducts);

        return "dashboard";
    }

    // Trang quản lý sản phẩm
    @GetMapping("/ui/products")
    public String products(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products";
    }

    @GetMapping("/ui/orders/create")
    public String createOrderPage(Model model) {
        // Truyền danh sách SP và KH sang để làm menu xổ xuống (Dropdown)
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("customers", customerService.getAllCustomers());

        // Tự động sinh mã đơn hàng ngẫu nhiên theo ngày (VD: DH260315-xxxx)
        String randomOrderCode = "DH" + LocalDate.now().toString().replace("-", "").substring(2) + "-" + (int)(Math.random() * 10000);
        model.addAttribute("defaultOrderCode", randomOrderCode);

        return "order-create";
    }

    // ... các code cũ giữ nguyên ...

    // Mở trang Thêm mới sản phẩm (Full Page)
    @GetMapping("/ui/products/create")
    public String createProductPage(Model model) {
        model.addAttribute("categories", masterDataService.getValuesByType("CATEGORY"));
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));
        return "product-create"; // Gọi file HTML mới
    }

    // Mở trang Danh sách Danh mục
    @GetMapping("/ui/categories")
    public String categoriesPage(Model model) {
        // Lấy danh sách danh mục từ bảng master_data
        model.addAttribute("categories", masterDataService.getAllByType("CATEGORY"));
        return "categories";
    }

    // Mở trang Thêm mới Danh mục
    @GetMapping("/ui/categories/create")
    public String createCategoryPage() {
        return "category-create";
    }

    @GetMapping("/ui/products/{sku}")
    public String editProductPage(@PathVariable String sku, Model model) {
        // Lấy dữ liệu sản phẩm từ DB lên
        model.addAttribute("product", productService.getProductBySku(sku));

        // Đổ Master Data ra các ô Dropdown
        model.addAttribute("categories", masterDataService.getValuesByType("CATEGORY"));
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));

        return "product-edit"; // Trỏ tới file HTML mới
    }
}