package com.oms.ui;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import com.oms.module.cashbook.dto.CashbookSummary;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.service.CashbookService;
import com.oms.module.category.service.CategoryService; // Thêm import này
import com.oms.module.customer.entity.Customer;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.inventory.dto.InventoryDTO;
import com.oms.module.inventory.service.InventoryService;
import com.oms.module.order.entity.Order;
import com.oms.module.order.service.OrderService;
import com.oms.module.product.service.ProductService;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.service.ReceiptService;
import com.oms.module.report.dto.ProfitReportResponse;
import com.oms.module.report.service.ReportService;
import com.oms.module.setting.entity.Branch;
import com.oms.module.setting.repository.BranchRepository;
import com.oms.module.setting.repository.SalesChannelRepository;
import com.oms.module.setting.service.MasterDataService;
import com.oms.module.supplier.service.SupplierService;
import com.oms.module.warranty.entity.WarrantyTicket;
import com.oms.module.warranty.repository.WarrantyTicketRepository;
import com.oms.module.warranty.service.WarrantyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductService productService;
    private final ReportService reportService;
    private final CustomerService customerService;
    private final MasterDataService masterDataService;
    private final SupplierService supplierService;
    private final ReceiptService receiptService;
    private final InventoryService inventoryService;

    private final OrderService orderService;
    // 1. INJECT THÊM CATEGORY SERVICE VÀO ĐÂY
    private final CategoryService categoryService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BranchRepository branchRepository;
    private final SalesChannelRepository channelRepository;
    private final CashbookService cashbookService;
    private final WarrantyService warrantyService;
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        // Lấy thông tin user đang đăng nhập từ username
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);

        // Kiểm tra mật khẩu cũ có khớp không
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không chính xác!");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam String fullName,
                                           @RequestParam(required = false) String email,
                                           @RequestParam(required = false) String phone,
                                           Principal principal) {
        // 1. Lấy user đầy đủ từ DB dựa trên username đang đăng nhập
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. CHỈ cập nhật các trường cho phép sửa từ form
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);

        // 3. Lưu lại (Lúc này các trường username, password, role vẫn được giữ nguyên)
        userRepository.save(user);

        return ResponseEntity.ok("Cập nhật thông tin thành công!");
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

    // Mở trang danh sách khách hàng
    @GetMapping("/ui/customers")
    public String listCustomersPage() {
        return "customers";
    }

    // Mở trang tạo mới khách hàng
    @GetMapping("/ui/customers/create")
    public String createCustomerPage() {
        return "customer-create";
    }

    // Mở trang chi tiết / sửa khách hàng
    @GetMapping("/ui/customers/{code}")
    public String editCustomerPage(@PathVariable String code, Model model) {
        model.addAttribute("customer", customerService.getCustomerByCode(code));
        List<Order> customerOrders = orderService.findTop10ByCustomer_CodeOrderByCreatedAtDescByCode(code);
        model.addAttribute("orders", customerOrders);
        return "customer-edit";
    }

    @GetMapping("/ui/customer-groups")
    public String listCustomerGroupsPage() {
        return "customer-groups";
    }

    @GetMapping("/ui/customer-groups/create")
    public String createCustomerGroupPage() {
        return "customer-group-create";
    }

    @GetMapping("/ui/customer-groups/{code}")
    public String editCustomerGroupPage(@PathVariable String code, Model model) {
        model.addAttribute("groupCode", code);
        return "customer-group-edit";
    }

    // 1. DANH SÁCH ĐƠN HÀNG
    @GetMapping("/ui/orders")
    public String showOrderListPage(Model model) {
        model.addAttribute("channels", channelRepository.findAll());
        return "order-list";
    }

    // 2. TẠO ĐƠN HÀNG (Phải đặt ưu tiên lên trước PathVariable)
    @GetMapping("/ui/orders/create")
    public String createOrderPage(Model model) {
        // Đã xóa dòng load getAllProducts() vì Frontend đã xử lý search động qua API

        // Sinh mã đơn hàng ngẫu nhiên gửi xuống UI
        String randomOrderCode = "DH" + LocalDate.now().toString().replace("-", "").substring(2) + "-" + (int) (Math.random() * 10000);
        model.addAttribute("defaultOrderCode", randomOrderCode);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("branches", branchRepository.findAll()); // Lấy chi nhánh
        model.addAttribute("channels", channelRepository.findAll()); // Lấy kênh bán hàng
        return "order-create";
    }

    // 3. CHI TIẾT ĐƠN HÀNG (Đặt xuống dưới cùng)
    @GetMapping("/ui/orders/detail/{orderCode}")
    public String orderDetailPage(@PathVariable String orderCode, Model model) {
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("channels", channelRepository.findAll());
        model.addAttribute("branches", branchRepository.findAll());
        return "order-detail";
    }


    // Mở trang Thêm mới sản phẩm
    @GetMapping("/ui/products/create")
    public String createProductPage(Model model) {
        // 3. ĐỔ DỮ LIỆU TỪ BẢNG MỚI RA
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));
        model.addAttribute("branches", branchRepository.findAll());
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
    public String showInventoryPage(
            @RequestParam(required = false) Long branchId, // Thêm tham số chọn chi nhánh
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(required = false) String dateRange,
            Model model) {

        // 1. Lấy danh sách tất cả Chi nhánh để đưa vào ComboBox chọn Kho
        List<Branch> branches = branchRepository.findAll();
        model.addAttribute("branches", branches);

        // 2. Xác định chi nhánh đang được chọn (Mặc định lấy chi nhánh đầu tiên nếu chưa chọn)
        Long selectedBranchId = branchId;
        if (selectedBranchId == null && !branches.isEmpty()) {
            selectedBranchId = branches.get(0).getId();
        }
        model.addAttribute("selectedBranchId", selectedBranchId);

        // 3. Gọi Service để lấy dữ liệu Tồn kho dựa trên branchId
        // LƯU Ý: Anh cần viết thêm hàm trong Service để map dữ liệu Inventory với Variant
        List<InventoryDTO> inventoryList = inventoryService.getInventoryList(
                selectedBranchId, keyword, stockStatus, minStock, maxStock, dateRange);

        model.addAttribute("inventories", inventoryList);

        // Giữ nguyên các tham số filter cũ
        model.addAttribute("keyword", keyword);
        model.addAttribute("stockStatus", stockStatus);
        model.addAttribute("minStock", minStock);
        model.addAttribute("maxStock", maxStock);
        model.addAttribute("dateRange", dateRange);

        return "inventory";
    }

    // Mở trang Danh sách nhà cung cấp
    @GetMapping("/ui/suppliers")
    public String listSuppliers(Model model, @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String assignee) {
        model.addAttribute("suppliers", supplierService.getSuppliers(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("assignee", assignee);
        return "suppliers";
    }

    // Mở trang Thêm mới nhà cung cấp
    @GetMapping("/ui/suppliers/create")
    public String createSupplierPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "supplier-create";
    }

    // Mở trang Chi tiết nhà cung cấp
    @GetMapping("/ui/suppliers/{code}")
    public String supplierDetailPage(@PathVariable String code, Model model) {
        model.addAttribute("supplier", supplierService.getSupplierByCode(code));
        model.addAttribute("users", userRepository.findAll());
        return "supplier-detail";
    }

    // Mở trang Nhập hàng (Tạo đơn nhập hàng)
    @GetMapping("/ui/imports/create")
    public String createImportPage(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));
        model.addAttribute("users", userRepository.findAll());

        model.addAttribute("branches", branchRepository.findAll());
        if (currentUser != null) {
            User userObj = userRepository.findByUsername(currentUser.getUsername()).orElse(null);
            model.addAttribute("currentUser", userObj);
        }
        return "import-create";
    }

    @GetMapping("/ui/imports")
    public String listImportsPage(Model model) {
        List<Receipt> receipts = receiptService.getAllReceipts();
        System.out.println("DEBUG: Số lượng đơn nhập lấy được = " + receipts.size());
        model.addAttribute("importOrders", receipts);

        return "import-list";
    }

    @GetMapping("/ui/imports/{code}")
    public String receiptDetailPage(@PathVariable String code, Model model) {
        // Gọi Service lấy thông tin phiếu nhập theo mã
        Receipt receipt = receiptService.getReceiptByCode(code);
        model.addAttribute("order", receipt);

        return "import-detail";
    }

    @GetMapping("/ui/imports/edit/{code}")
    public String editImportPage(@PathVariable String code, Model model) {
        Receipt receipt = receiptService.getReceiptByCode(code);

        // Chỉ cho phép sửa khi đơn chưa hoàn thành và chưa nhập kho
        if ("COMPLETED".equals(receipt.getStatus()) || "COMPLETED".equals(receipt.getImportStatus())) {
            return "redirect:/ui/imports/" + code; // Nếu cố tình vào thì đá về trang chi tiết
        }

        model.addAttribute("order", receipt);
        return "import-edit"; // Mở file import-edit.html
    }


    @GetMapping("/ui/cashbook")
    public String cashbookOverview(
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "today") String preset,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        LocalDateTime now = LocalDateTime.now();

        // 1. TÍNH TOÁN NGÀY THÁNG DỰA TRÊN PRESET (Fix lỗi MySQL làm tròn ngày)
        if ("custom".equals(preset)) {
            // Tùy chọn: Set cứng Nano về 0 để tránh MySQL tự làm tròn
            if (start == null) start = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (end == null) end = now.withHour(23).withMinute(59).withSecond(59).withNano(0);
        } else {
            switch (preset) {
                case "yesterday":
                    start = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    end = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);
                    break;
                case "thisMonth":
                    start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    end = now.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
                    break;
                case "lastMonth":
                    start = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    end = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(0);
                    break;
                case "thisYear":
                    start = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                    end = now.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59).withNano(0);
                    break;
                case "today":
                default:
                    start = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
                    end = now.withHour(23).withMinute(59).withSecond(59).withNano(0);
                    break;
            }
        }

        // 2. GỌI XUỐNG SERVICE
        List<CashTransaction> filteredTransactions = cashbookService.filterTransactions(keyword, branchId, type, start, end);

        // 3. ĐẨY DATA RA VIEW
        model.addAttribute("transactions", filteredTransactions);
        model.addAttribute("summary", cashbookService.getSummary(start, end));
        model.addAttribute("branches", branchRepository.findAll());

        CashbookSummary summaryData = cashbookService.getSummary(start, end, branchId);
        model.addAttribute("summary", summaryData);

        // 3. Đẩy danh sách chi nhánh ra Dropdown
        model.addAttribute("branches", branchRepository.findAll());
        return "cashbook";
    }

    @GetMapping("/ui/cashbook/receipt/create")
    public String createReceiptPage(Model model) {
        model.addAttribute("branches", branchRepository.findAll());
        return "cash-receipt-create";
    }

    @GetMapping("/ui/cashbook/payment/create")
    public String createPaymentPage(Model model) {
        model.addAttribute("branches", branchRepository.findAll());
        return "cash-payment-create";
    }

    @GetMapping("/ui/cashbook/detail/{id}")
    public String transactionDetail(@PathVariable Long id, Model model) {
        CashTransaction transaction = cashbookService.getById(id);
        model.addAttribute("transaction", transaction);
        if (transaction.getType().name().equals("RECEIPT")) {
            return "cash-receipt-detail";
        }
        return "cash-payment-detail";
    }

    @GetMapping("/ui/warranties")
    public String warrantyList(
            Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        List<WarrantyTicket> tickets = warrantyService.filterTickets(keyword, status, type);
        model.addAttribute("tickets", tickets);
        return "warranty/warranty-list";
    }
    @GetMapping("/ui/warranties/create")
    public String createWarrantyForm(Model model) {
        // Gửi danh sách chi nhánh xuống cho nhân viên chọn nơi nhận máy
        model.addAttribute("branches", branchRepository.findAll());
        return "warranty/warranty-create";
    }

    @GetMapping("/ui/warranties/detail/{id}")
    public String warrantyDetail(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", warrantyService.getById(id));
        model.addAttribute("branches", branchRepository.findAll());

        WarrantyTicket ticket = warrantyService.getById(id);
        if (ticket.getBranchId() != null) {
            branchRepository.findById(ticket.getBranchId()).ifPresent(branch -> {
                model.addAttribute("receivingBranchName", branch.getName());
                model.addAttribute("receivingBranchAddress", branch.getAddress());
            });
        }
        return "warranty/warranty-detail";
    }
}