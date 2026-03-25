package com.oms.ui;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductService productService;
    private final ReportService reportService; // Thêm ReportService
    private final CustomerService customerService;
    private final MasterDataService masterDataService;
    private final SupplierService supplierService;
    private final ReceiptService receiptService;


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
//    @GetMapping("/ui/products")
//    public String products(Model model) {
//        model.addAttribute("products", productService.getAllProducts());
//        return "products";
//    }

    @GetMapping("/ui/products")
    public String listProductsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            Model model) {

        // 1. Lấy danh sách sản phẩm theo bộ lọc
        model.addAttribute("products", productService.getFilteredProducts(keyword, category, brand));

        // 2. Lấy Master Data để vẽ Dropdown Lọc
        model.addAttribute("categories", masterDataService.getValuesByType("CATEGORY"));
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));

        // 3. Đẩy lại tham số ra View để giữ trạng thái đã chọn (Không bị reset Form)
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);

        return "products";
    }

    @GetMapping("/ui/orders/create")
    public String createOrderPage(Model model) {
        // Truyền danh sách SP và KH sang để làm menu xổ xuống (Dropdown)
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("customers", customerService.getAllCustomers());

        // Tự động sinh mã đơn hàng ngẫu nhiên theo ngày (VD: DH260315-xxxx)
        String randomOrderCode = "DH" + LocalDate.now().toString().replace("-", "").substring(2) + "-" + (int) (Math.random() * 10000);
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

    @GetMapping("/ui/inventory")
    public String inventoryPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(required = false) String dateRange,
            Model model) {

        // Gọi Service lấy dữ liệu đã lọc
        model.addAttribute("variants", productService.getFilteredInventory(keyword, stockStatus, minStock, maxStock, dateRange));

        // Trả lại tham số ra View để giữ trạng thái Form (Lọc xong không bị mất số đã gõ)
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
        // Sau này load danh sách NCC, Chi nhánh truyền xuống đây
        return "import-create"; // Đổi tên file HTML thành import-create.html cho chuẩn
    }

    @GetMapping("/ui/imports")
    public String listImportsPage(Model model) {
        // 1. Gọi service để lấy danh sách từ Database
        List<Receipt> receipts = receiptService.getAllReceipts();

        // 2. Log ra console để kiểm tra xem DB đã có dữ liệu chưa
        System.out.println("DEBUG: Số lượng đơn nhập lấy được = " + receipts.size());

        // 3. Đưa vào model với tên biến ĐÚNG NHƯ TRONG FILE HTML (importOrders)
        model.addAttribute("importOrders", receipts);

        return "import-list"; // Tên file HTML của ông
    }

    @GetMapping("/ui/imports/{id}")
    public String detailImportPage(@PathVariable Long id, Model model) {
        // Lấy đơn nhập kèm Supplier và Details
        Receipt order = receiptService.getReceiptById(id);
        model.addAttribute("order", order);
        return "import-detail";
    }
}