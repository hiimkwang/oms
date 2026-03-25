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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {


    @Value("${app.upload.dir:/home/oms/}")
    private String uploadDir;

    @Value("${app.upload.domain:https://oms.mechkey.vn/}")
    private String domain;


    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            String subFolder = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFileName = "img_" + System.currentTimeMillis() + extension;

            Path folderPath = Paths.get(uploadDir, dateFolder, subFolder);
            if (!Files.exists(folderPath)) Files.createDirectories(folderPath);

            Path filePath = folderPath.resolve(newFileName);
            file.transferTo(filePath.toFile());

            // Trả về link để lưu vào DB
            return domain + dateFolder + "/" + subFolder + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage());
        }
    }
}