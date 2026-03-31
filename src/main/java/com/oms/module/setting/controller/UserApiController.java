package com.oms.module.setting.controller;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import com.oms.module.setting.entity.Branch;
import com.oms.module.setting.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder; // Bắt buộc phải inject cái này để mã hóa pass

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username").trim();

        // 1. Kiểm tra xem username đã tồn tại chưa
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Lỗi: Tên đăng nhập này đã tồn tại trong hệ thống!");
        }

        // 2. Tạo User mới
        User user = new User();
        user.setFullName(payload.get("fullName"));
        user.setUsername(username);
        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(payload.get("password")));

        // CHỈNH SỬA Ở ĐÂY: Ép kiểu String sang Enum Role
        try {
            user.setRole(User.Role.valueOf(payload.get("role")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Lỗi: Quyền hạn không hợp lệ!");
        }

        user.setActive(true);

        // 3. Gắn vào chi nhánh (nếu có chọn)
        String branchIdStr = payload.get("branchId");
        if (branchIdStr != null && !branchIdStr.isEmpty()) {
            Branch branch = branchRepository.findById(Long.parseLong(branchIdStr)).orElse(null);
            user.setBranch(branch);
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Tạo tài khoản nhân viên thành công!"));
    }
}