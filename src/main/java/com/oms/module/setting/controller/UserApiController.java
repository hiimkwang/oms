package com.oms.module.setting.controller;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import com.oms.module.setting.entity.Branch;
import com.oms.module.setting.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        String usernameRaw = payload.get("username");
        String password = payload.get("password");
        if (usernameRaw == null || usernameRaw.isBlank()) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập không được để trống!");
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Lỗi: Mật khẩu không được để trống!");
        }
        String username = usernameRaw.trim();

        // 1. Kiểm tra xem username đã tồn tại chưa
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập này đã tồn tại trong hệ thống!");
        }

        // 2. Tạo User mới
        User user = new User();
        user.setFullName(payload.get("fullName"));
        user.setUsername(username);
        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(password));

        String roleStr = payload.get("role");
        if (roleStr == null || roleStr.isBlank()) {
            return ResponseEntity.badRequest().body("Lỗi: Vui lòng chọn quyền hạn!");
        }
        try {
            user.setRole(User.Role.valueOf(roleStr));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: Quyền hạn không hợp lệ!");
        }

        user.setActive(true);

        // 3. Gắn vào chi nhánh (nếu có chọn)
        String branchIdStr = payload.get("branchId");
        if (branchIdStr != null && !branchIdStr.isEmpty()) {
            try {
                Branch branch = branchRepository.findById(Long.parseLong(branchIdStr)).orElse(null);
                user.setBranch(branch);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("Lỗi: Chi nhánh không hợp lệ!");
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Tạo tài khoản nhân viên thành công!"));
    }
}