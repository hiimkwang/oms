package com.oms.module.setting.config; // Đổi lại package theo project của ông

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:/home/oms/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình đường dẫn tuyệt đối cho Spring Boot
        String path = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";

        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + path); // Dùng "file:" để trỏ ra ổ cứng vật lý
    }
}