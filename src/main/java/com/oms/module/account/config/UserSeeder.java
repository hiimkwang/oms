package com.oms.module.account.config;

import com.oms.module.account.entity.User;
import com.oms.module.account.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setFullName("Quản trị viên");
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            //System.out.println("Đã tạo tài khoản admin mặc định: admin / 123456");
        }
    }
}