package com.oms.ui;

import com.oms.module.category.service.CategoryService; // Thêm import này
import com.oms.module.customer.service.CustomerService;
import com.oms.module.product.repository.ProductVariantRepository;
import com.oms.module.product.service.ProductService;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.service.ReceiptService;
import com.oms.module.report.dto.ProfitReportResponse;
import com.oms.module.report.service.ReportService;
import com.oms.module.setting.service.MasterDataService;
import com.oms.module.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductService productService;
    private final ReportService reportService;
    private final CustomerService customerService;
    private final MasterDataService masterDataService;
    private final SupplierService supplierService;
    private final ReceiptService receiptService;

    // 1. INJECT THÊM CATEGORY SERVICE VÀO ĐÂY
    private final CategoryService categoryService;

    // Trang chủ - Bảng điều khiển (Dashboard)
    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        ProfitReportResponse report = reportService.getMonthlyProfitReport(currentMonth, currentYear);

        model.addAttribute("report", report);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentYear);

        int totalProducts = productService.getAllProducts().size();
        model.addAttribute("totalProducts", totalProducts);

        return "dashboard";
    }

    // Trang quản lý sản phẩm
    @GetMapping("/ui/products")
    public String listProductsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long category, // Nhận Long categoryId
            @RequestParam(required = false) String brand,
            Model model) {

        model.addAttribute("products", productService.getFilteredProducts(keyword, category, brand));

        // 2. SỬA LẠI CÁCH LẤY DANH MỤC
        model.addAttribute("categories", categoryService.getAll()); // Dùng bảng Category mới
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND")); // Hãng vẫn dùng Master Data

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);

        return "products";
    }

    @GetMapping("/ui/orders/create")
    public String createOrderPage(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("customers", customerService.getAllCustomers());

        String randomOrderCode = "DH" + LocalDate.now().toString().replace("-", "").substring(2) + "-" + (int) (Math.random() * 10000);
        model.addAttribute("defaultOrderCode", randomOrderCode);

        return "order-create";
    }

    // Mở trang Thêm mới sản phẩm
    @GetMapping("/ui/products/create")
    public String createProductPage(Model model) {
        // 3. ĐỔ DỮ LIỆU TỪ BẢNG MỚI RA
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));
        return "product-create";
    }

    // Mở trang Danh sách Danh mục
    @GetMapping("/ui/categories")
    public String categoriesPage(Model model) {
        // 4. LẤY TỪ CATEGORY SERVICE VÀ SỬA TÊN VIEW
        model.addAttribute("categories", categoryService.getAll());
        return "category-list"; // Đổi thành category-list cho chuẩn với HTML anh em mình làm
    }

    // Mở trang Thêm mới Danh mục
    @GetMapping("/ui/categories/create")
    public String createCategoryPage() {
        return "category-create";
    }
    // Mở trang Chi tiết / Sửa Danh mục
    @GetMapping("/ui/categories/edit/{id}")
    public String editCategoryPage(@PathVariable Long id, Model model) {
        // Lấy thông tin danh mục từ DB theo ID
        model.addAttribute("category", categoryService.getById(id));

        return "category-edit"; // Trỏ đúng tên file category-edit.html anh em vừa tạo
    }
    // API Xóa hàng loạt
    @PostMapping("/ui/categories/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestBody List<Long> ids) {
        try {
            categoryService.bulkDelete(ids);
            return ResponseEntity.ok().body("Xóa thành công " + ids.size() + " danh mục");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }
    // Sửa sản phẩm
    @GetMapping("/ui/products/{sku}")
    public String editProductPage(@PathVariable String sku, Model model) {
        model.addAttribute("product", productService.getProductBySku(sku));

        // 5. CẬP NHẬT CHỖ NÀY NỮA
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));

        return "product-edit";
    }

    @GetMapping("/ui/inventory")
    public String inventoryPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(required = false) String dateRange,
            Model model) {

        model.addAttribute("variants", productService.getFilteredInventory(keyword, stockStatus, minStock, maxStock, dateRange));

        model.addAttribute("keyword", keyword);
        model.addAttribute("stockStatus", stockStatus);
        model.addAttribute("minStock", minStock);
        model.addAttribute("maxStock", maxStock);
        model.addAttribute("dateRange", dateRange);

        return "inventory";
    }

    // Mở trang Danh sách nhà cung cấp
    @GetMapping("/ui/suppliers")
    public String suppliersPage(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("suppliers", supplierService.getSuppliers(keyword));
        model.addAttribute("keyword", keyword);
        return "suppliers";
    }

    // Mở trang Thêm mới nhà cung cấp
    @GetMapping("/ui/suppliers/create")
    public String createSupplierPage() {
        return "supplier-create";
    }

    // Mở trang Chi tiết nhà cung cấp
    @GetMapping("/ui/suppliers/{code}")
    public String supplierDetailPage(@PathVariable String code, Model model) {
        model.addAttribute("supplier", supplierService.getSupplierByCode(code));
        return "supplier-detail";
    }

    // Mở trang Nhập hàng (Tạo đơn nhập hàng)
    @GetMapping("/ui/imports/create")
    public String createImportPage(Model model) {
        // PHẢI CÓ 3 DÒNG NÀY THÌ MODAL MỚI CÓ DATA ĐỂ CHỌN
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));

        return "import-create";
    }

    @GetMapping("/ui/imports")
    public String listImportsPage(Model model) {
        List<Receipt> receipts = receiptService.getAllReceipts();
        System.out.println("DEBUG: Số lượng đơn nhập lấy được = " + receipts.size());
        model.addAttribute("importOrders", receipts);

        return "import-list";
    }

    @GetMapping("/ui/imports/{id}")
    public String detailImportPage(@PathVariable Long id, Model model) {
        Receipt order = receiptService.getReceiptById(id);
        model.addAttribute("order", order);
        return "import-detail";
    }
}