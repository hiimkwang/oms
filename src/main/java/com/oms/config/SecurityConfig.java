package com.oms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Bật @PreAuthorize ở tầng method (kiểm soát truy cập chi tiết)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/vendors/**").permitAll()
                        // Trang quản lý calo & gym: truy cập tự do qua URL riêng (không cần đăng nhập OMS)
                        .requestMatchers("/gym", "/api/gym/**").permitAll()
                        // Ảnh upload (ảnh bài tập gym...) cho phép xem không cần đăng nhập
                        .requestMatchers("/media/**").permitAll()
                        // Trang lỗi mặc định: cho phép để các trang công khai (gym) báo lỗi gọn gàng
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/ui/settings/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/settings/**").hasRole("ADMIN")

                        // Các trang còn lại (Bán hàng, Đơn hàng, Dashboard...) thì cả STAFF và ADMIN đều được vào
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Bật lại CSRF: token được lưu vào cookie (đọc được bởi JS qua meta tag/cookie)
                // để các lời gọi fetch và form POST kèm theo X-XSRF-TOKEN / _csrf.
                // Riêng API gym (không đăng nhập, không dùng session) thì bỏ qua CSRF.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/gym/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}