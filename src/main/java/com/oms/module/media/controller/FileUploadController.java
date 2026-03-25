package com.oms.module.media.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Value("${app.upload.dir:/home/oms/}")
    private String uploadDir;

    @Value("${app.upload.domain:https://oms.mechkey.vn/}")
    private String domain;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Lấy ngày hiện tại format: 25032026
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

            // 2. Tạo subfolder ngẫu nhiên 4 ký tự (VD: 0A3F, 0001) để tránh trùng lặp
            String subFolder = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            // 3. Tách lấy đuôi file gốc (VD: .webp, .png, .jpg)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

            // Tên file mới: img_timestamp.webp
            String newFileName = "img_" + System.currentTimeMillis() + extension;

            // 4. Tạo cây thư mục vật lý (Nếu chưa có thì tự động tạo)
            Path folderPath = Paths.get(uploadDir, dateFolder, subFolder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // 5. Chuyển file từ RAM xuống Ổ cứng VPS
            Path filePath = folderPath.resolve(newFileName);
            file.transferTo(filePath.toFile());

            // 6. Nối chuỗi trả về link public cho Frontend
            // Output: https://oms.mechkey.vn/25032026/0A3F/img_1711330000.webp
            String fileUrl = domain + dateFolder + "/" + subFolder + "/" + newFileName;

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi lưu file vào ổ cứng: " + e.getMessage()));
        }
    }
}