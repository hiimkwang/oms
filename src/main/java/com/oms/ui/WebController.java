package com.oms.ui;

import com.oms.module.account.entity.User;
import com.oms.module.account.service.CustomUserDetailsService;
import com.oms.module.cashbook.dto.CashbookSummary;
import com.oms.module.cashbook.entity.CashTransaction;
import com.oms.module.cashbook.service.CashbookService;
import com.oms.module.category.service.CategoryService;
import com.oms.module.customer.service.CustomerService;
import com.oms.module.inventory.dto.InventoryDTO;
import com.oms.module.inventory.service.InventoryService;
import com.oms.module.notification.service.NotificationService;
import com.oms.module.order.entity.Order;
import com.oms.module.order.service.OrderService;
import com.oms.module.product.service.ProductService;
import com.oms.module.receipt.entity.Receipt;
import com.oms.module.receipt.service.ReceiptService;
import com.oms.module.returnorder.entity.ReturnOrder;
import com.oms.module.returnorder.service.ReturnOrderService;
import com.oms.module.setting.entity.Branch;
import com.oms.module.setting.service.BranchService;
import com.oms.module.setting.service.MasterDataService;
import com.oms.module.setting.service.SalesChannelService;
import com.oms.module.supplier.service.SupplierService;
import com.oms.module.warranty.entity.WarrantyTicket;
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
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductService productService;
    private final CustomerService customerService;
    private final MasterDataService masterDataService;
    private final SupplierService supplierService;
    private final ReceiptService receiptService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;
    private final BranchService branchService;
    private final SalesChannelService salesChannelService;
    private final CashbookService cashbookService;
    private final WarrantyService warrantyService;
    private final CustomUserDetailsService customUserDetailsService;
    private final ReturnOrderService returnOrderService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        User user = customUserDetailsService.findByUsername(username).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String oldPassword,
                                            @RequestParam String newPassword,
                                            Principal principal) {
        User user = customUserDetailsService.findByUsername(principal.getName()).orElse(null);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu cũ không chính xác!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        customUserDetailsService.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestParam String fullName,
                                           @RequestParam(required = false) String email,
                                           @RequestParam(required = false) String phone,
                                           Principal principal) {
        User user = customUserDetailsService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);

        customUserDetailsService.save(user);

        return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

    @GetMapping("/ui/products")
    public String listProductsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long category, // Nhận Long categoryId
            @RequestParam(required = false) String brand,
            Model model) {

        model.addAttribute("products", productService.getFilteredProducts(keyword, category, brand));

        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);

        return "product/products";
    }

    @GetMapping("/ui/products/create")
    public String createProductPage(Model model) {
        // 3. ĐỔ DỮ LIỆU TỪ BẢNG MỚI RA
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));
        model.addAttribute("branches", branchService.findAll());
        return "product/product-create";
    }

    @GetMapping("/ui/products/{sku}")
    public String editProductPage(@PathVariable String sku, Model model) {
        model.addAttribute("product", productService.getProductBySku(sku));

        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));

        return "product/product-edit";
    }

    @GetMapping("/ui/customers")
    public String listCustomersPage() {
        return "customer/customers";
    }

    @GetMapping("/ui/customers/create")
    public String createCustomerPage() {
        return "customer/customer-create";
    }

    @GetMapping("/ui/customers/{code}")
    public String editCustomerPage(@PathVariable String code, Model model) {
        model.addAttribute("customer", customerService.getCustomerByCode(code));
        List<Order> customerOrders = orderService.findTop10ByCustomer_CodeOrderByCreatedAtDescByCode(code);
        model.addAttribute("orders", customerOrders);
        return "customer/customer-edit";
    }

    @GetMapping("/ui/customer-groups")
    public String listCustomerGroupsPage() {
        return "customer/customer-groups";
    }

    @GetMapping("/ui/customer-groups/create")
    public String createCustomerGroupPage() {
        return "customer/customer-group-create";
    }

    @GetMapping("/ui/customer-groups/{code}")
    public String editCustomerGroupPage(@PathVariable String code, Model model) {
        model.addAttribute("groupCode", code);
        return "customer/customer-group-edit";
    }

    @GetMapping("/ui/orders")
    public String showOrderListPage(Model model) {
        model.addAttribute("channels", salesChannelService.findAll());
        return "order/order-list";
    }

    @GetMapping("/ui/orders/create")
    public String createOrderPage(Model model) {

        String randomOrderCode = "DH" + LocalDate.now().toString().replace("-", "").substring(2) + "-" + (int) (Math.random() * 10000);
        model.addAttribute("defaultOrderCode", randomOrderCode);
        model.addAttribute("users", customUserDetailsService.findAll());
        model.addAttribute("branches", branchService.findAll());
        model.addAttribute("channels", salesChannelService.findAll());
        return "order/order-create";
    }

    @GetMapping("/ui/orders/detail/{orderCode}")
    public String orderDetailPage(@PathVariable String orderCode, Model model) {
        model.addAttribute("orderCode", orderCode);
        model.addAttribute("channels", salesChannelService.findAll());
        model.addAttribute("branches", branchService.findAll());
        return "order/order-detail";
    }


    @GetMapping("/ui/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "category/category-list";
    }

    @GetMapping("/ui/categories/create")
    public String createCategoryPage() {
        return "category/category-create";
    }

    @GetMapping("/ui/categories/edit/{id}")
    public String editCategoryPage(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getById(id));

        return "category/category-edit";
    }

    @PostMapping("/ui/categories/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestBody List<Long> ids) {
        try {
            categoryService.bulkDelete(ids);
            return ResponseEntity.ok().body("Xóa thành công " + ids.size() + " danh mục");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi xóa: " + e.getMessage());
        }
    }


    @GetMapping("/ui/inventory")
    public String showInventoryPage(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(required = false) String dateRange,
            Model model) {

        List<Branch> branches = branchService.findAll();
        model.addAttribute("branches", branches);

        Long selectedBranchId = branchId;
        if (selectedBranchId == null && !branches.isEmpty()) {
            selectedBranchId = branches.get(0).getId();
        }
        model.addAttribute("selectedBranchId", selectedBranchId);

        List<InventoryDTO> inventoryList = inventoryService.getInventoryList(
                selectedBranchId, keyword, stockStatus, minStock, maxStock, dateRange);

        model.addAttribute("inventories", inventoryList);

        model.addAttribute("keyword", keyword);
        model.addAttribute("stockStatus", stockStatus);
        model.addAttribute("minStock", minStock);
        model.addAttribute("maxStock", maxStock);
        model.addAttribute("dateRange", dateRange);

        return "inventory/inventory";
    }

    @GetMapping("/ui/suppliers")
    public String listSuppliers(Model model, @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String assignee) {
        model.addAttribute("suppliers", supplierService.getSuppliers(keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("users", customUserDetailsService.findAll());
        model.addAttribute("assignee", assignee);
        return "supplier/suppliers";
    }

    @GetMapping("/ui/suppliers/create")
    public String createSupplierPage(Model model) {
        model.addAttribute("users", customUserDetailsService.findAll());
        return "supplier/supplier-create";
    }

    @GetMapping("/ui/suppliers/{code}")
    public String supplierDetailPage(@PathVariable String code, Model model) {
        model.addAttribute("supplier", supplierService.getSupplierByCode(code));
        model.addAttribute("users", customUserDetailsService.findAll());
        return "supplier/supplier-detail";
    }

    @GetMapping("/ui/imports/create")
    public String createImportPage(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", masterDataService.getValuesByType("BRAND"));
        model.addAttribute("units", masterDataService.getValuesByType("UNIT"));
        model.addAttribute("users", customUserDetailsService.findAll());

        model.addAttribute("branches", branchService.findAll());
        if (currentUser != null) {
            User userObj = customUserDetailsService.findByUsername(currentUser.getUsername()).orElse(null);
            model.addAttribute("currentUser", userObj);
        }
        return "import/import-create";
    }

    @GetMapping("/ui/imports")
    public String listImportsPage(Model model) {
        List<Receipt> receipts = receiptService.getAllReceipts();
        model.addAttribute("importOrders", receipts);

        return "import/import-list";
    }

    @GetMapping("/ui/imports/{code}")
    public String receiptDetailPage(@PathVariable String code, Model model) {
        Receipt receipt = receiptService.getReceiptByCode(code);
        model.addAttribute("order", receipt);

        return "import/import-detail";
    }

    @GetMapping("/ui/imports/edit/{code}")
    public String editImportPage(@PathVariable String code, Model model) {
        Receipt receipt = receiptService.getReceiptByCode(code);

        if ("COMPLETED".equals(receipt.getStatus()) || "COMPLETED".equals(receipt.getImportStatus())) {
            return "redirect:/ui/imports/" + code;
        }

        model.addAttribute("order", receipt);
        model.addAttribute("branches", branchService.findAll());
        return "import/import-edit";
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

        if ("custom".equals(preset)) {
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

        List<CashTransaction> filteredTransactions = cashbookService.filterTransactions(keyword, branchId, type, start, end);

        model.addAttribute("transactions", filteredTransactions);
        model.addAttribute("summary", cashbookService.getSummary(start, end));
        model.addAttribute("branches", branchService.findAll());

        CashbookSummary summaryData = cashbookService.getSummary(start, end, branchId);
        model.addAttribute("summary", summaryData);

        model.addAttribute("branches", branchService.findAll());
        return "cashbook/cashbook";
    }

    @GetMapping("/ui/cashbook/receipt/create")
    public String createReceiptPage(Model model) {
        model.addAttribute("branches", branchService.findAll());
        return "cashbook/cash-receipt-create";
    }

    @GetMapping("/ui/cashbook/payment/create")
    public String createPaymentPage(Model model) {
        model.addAttribute("branches", branchService.findAll());
        return "cashbook/cash-payment-create";
    }

    @GetMapping("/ui/cashbook/detail/{id}")
    public String transactionDetail(@PathVariable Long id, Model model) {
        CashTransaction transaction = cashbookService.getById(id);
        model.addAttribute("transaction", transaction);
        if (transaction.getType().name().equals("RECEIPT")) {
            return "cashbook/cash-receipt-detail";
        }
        return "cashbook/cash-payment-detail";
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
        model.addAttribute("branches", branchService.findAll());
        return "warranty/warranty-create";
    }

    @GetMapping("/ui/warranties/detail/{id}")
    public String warrantyDetail(@PathVariable Long id, Model model) {
        model.addAttribute("ticket", warrantyService.getById(id));
        model.addAttribute("branches", branchService.findAll());

        WarrantyTicket ticket = warrantyService.getById(id);
        if (ticket.getBranchId() != null) {
            branchService.findById(ticket.getBranchId()).ifPresent(branch -> {
                model.addAttribute("receivingBranchName", branch.getName());
                model.addAttribute("receivingBranchAddress", branch.getAddress());
            });
        }
        return "warranty/warranty-detail";
    }


    @GetMapping("/ui/notifications")
    public String list(Model model) {
        model.addAttribute("notifications", notificationService.getAll());
        return "notifications/list-noti";
    }

    @GetMapping("/ui/returns")
    public String getReturnList(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                Model model) {
        List<ReturnOrder> returns = returnOrderService.getAllReturns(keyword, status);
        model.addAttribute("returns", returns);
        return "returnorder/return-list";
    }

    @GetMapping("/ui/returns/{code}")
    public String getReturnDetail(@PathVariable String code, Model model) {
        ReturnOrder returnOrder = returnOrderService.getByCode(code);

        model.addAttribute("returnOrder", returnOrder);

        model.addAttribute("branches", branchService.findAll());

        return "returnorder/return-detail";
    }
}