package com.oms.module.account.config;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Mật khẩu admin khởi tạo: đọc từ biến môi trường APP_SEED_ADMIN_PASSWORD (KHÔNG hardcode).
    // Nếu để trống, hệ thống sinh mật khẩu ngẫu nhiên và in ra log 1 lần khi seed.
    @Value("${app.seed.admin-password:}")
    private String seedAdminPassword;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            String rawPassword = (seedAdminPassword != null && !seedAdminPassword.isBlank())
                    ? seedAdminPassword.trim()
                    : UUID.randomUUID().toString().substring(0, 12);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(rawPassword));
            admin.setFullName("Quản trị viên");
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            if (seedAdminPassword == null || seedAdminPassword.isBlank()) {
                log.warn("========================================================================");
                log.warn("  ĐÃ TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH");
                log.warn("  Username: admin");
                log.warn("  Password (ngẫu nhiên - HÃY ĐỔI NGAY sau khi đăng nhập): {}", rawPassword);
                log.warn("  (Đặt biến môi trường APP_SEED_ADMIN_PASSWORD để tự chọn mật khẩu.)");
                log.warn("========================================================================");
            } else {
                log.info("Đã tạo tài khoản admin với mật khẩu từ cấu hình APP_SEED_ADMIN_PASSWORD.");
            }
        }
    }
}
