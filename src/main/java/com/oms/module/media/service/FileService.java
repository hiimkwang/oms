package com.oms.module.media.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    // Chỉ cho phép upload ảnh, giới hạn dung lượng để tránh lạm dụng / file độc hại
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.dir:/home/oms/}")
    private String uploadDir;

    @Value("${app.upload.domain:https://oms.mechkey.vn/}")
    private String domain;


    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File vượt quá dung lượng cho phép (tối đa 5MB).");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT) : "";

        String contentType = file.getContentType();
        boolean validExt = ALLOWED_EXTENSIONS.contains(extension);
        boolean validType = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
        if (!validExt || !validType) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp).");
        }

        try {
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            String subFolder = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            // Tên file sinh hoàn toàn ở server (không dùng tên gốc) để tránh path traversal
            String newFileName = "img_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Path baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path folderPath = baseDir.resolve(Paths.get(dateFolder, subFolder)).normalize();
            // Chặn path traversal: thư mục đích phải nằm trong baseDir
            if (!folderPath.startsWith(baseDir)) {
                throw new IllegalArgumentException("Đường dẫn lưu file không hợp lệ.");
            }
            if (!Files.exists(folderPath)) Files.createDirectories(folderPath);

            Path filePath = folderPath.resolve(newFileName);
            file.transferTo(filePath.toFile());

            return domain + dateFolder + "/" + subFolder + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file.");
        }
    }
}