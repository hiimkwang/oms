package com.oms.module.setting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sao lưu database MySQL bằng mysqldump.
 * Cấu hình tại application.properties: app.backup.*
 */
@Service
@Slf4j
public class BackupService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${app.backup.enabled:true}")
    private boolean enabled;

    @Value("${app.backup.dir:backups}")
    private String backupDir;

    @Value("${app.backup.mysqldump-path:mysqldump}")
    private String mysqldumpPath;

    @Value("${app.backup.keep-days:14}")
    private int keepDays;

    public boolean isEnabled() {
        return enabled;
    }

    public String getBackupDir() {
        return backupDir;
    }

    /**
     * Thực hiện sao lưu ngay. Trả về kết quả gồm trạng thái + mô tả.
     */
    public BackupResult performBackup() {
        try {
            DbInfo db = parseJdbcUrl(datasourceUrl);
            if (db == null) {
                return new BackupResult(false, null, "Không đọc được thông tin database từ cấu hình.", 0);
            }

            File dir = new File(backupDir);
            if (!dir.exists() && !dir.mkdirs()) {
                return new BackupResult(false, null, "Không tạo được thư mục sao lưu: " + backupDir, 0);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "oms_" + db.database + "_" + timestamp + ".sql";
            File outFile = new File(dir, fileName);

            List<String> command = new ArrayList<>();
            command.add(mysqldumpPath);
            command.add("--host=" + db.host);
            command.add("--port=" + db.port);
            command.add("--user=" + dbUser);
            // KHÔNG truyền mật khẩu qua dòng lệnh (lộ trong danh sách tiến trình).
            // Dùng biến môi trường MYSQL_PWD mà mysqldump tự đọc.
            command.add("--default-character-set=utf8mb4");
            command.add("--single-transaction"); // không khóa bảng InnoDB khi sao lưu
            command.add("--routines");
            command.add("--result-file=" + outFile.getAbsolutePath());
            command.add(db.database);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().put("MYSQL_PWD", dbPassword != null ? dbPassword : "");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String stderr = new String(process.getInputStream().readAllBytes());
            boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                return new BackupResult(false, fileName, "Quá thời gian sao lưu (10 phút).", 0);
            }

            int exit = process.exitValue();
            if (exit != 0 || !outFile.exists() || outFile.length() == 0) {
                if (outFile.exists() && outFile.length() == 0) outFile.delete();
                return new BackupResult(false, fileName,
                        "mysqldump lỗi (mã " + exit + "). " + (stderr.isBlank() ? "Kiểm tra đường dẫn mysqldump." : stderr.trim()), 0);
            }

            long sizeKb = outFile.length() / 1024;
            cleanupOldBackups(dir);
            log.info("Sao lưu database thành công: {} ({} KB)", fileName, sizeKb);
            return new BackupResult(true, fileName, "Sao lưu thành công.", sizeKb);

        } catch (Exception e) {
            log.error("Lỗi sao lưu database: {}", e.getMessage(), e);
            return new BackupResult(false, null, "Lỗi: " + e.getMessage(), 0);
        }
    }

    private void cleanupOldBackups(File dir) {
        if (keepDays <= 0) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files == null) return;
        Instant cutoff = Instant.now().minus(Duration.ofDays(keepDays));
        for (File f : files) {
            if (Instant.ofEpochMilli(f.lastModified()).isBefore(cutoff)) {
                if (f.delete()) log.info("Đã xóa bản sao lưu cũ: {}", f.getName());
            }
        }
    }

    public List<BackupFile> listBackups() {
        List<BackupFile> result = new ArrayList<>();
        File dir = new File(backupDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));
        if (files == null) return result;
        for (File f : files) {
            result.add(new BackupFile(f.getName(), f.length() / 1024,
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), java.time.ZoneId.systemDefault())));
        }
        result.sort(Comparator.comparing(BackupFile::createdAt).reversed());
        return result;
    }

    private DbInfo parseJdbcUrl(String url) {
        // jdbc:mysql://host:port/dbname?params
        Pattern p = Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?;]+)");
        Matcher m = p.matcher(url);
        if (!m.find()) return null;
        DbInfo info = new DbInfo();
        info.host = m.group(1);
        info.port = m.group(2) != null ? m.group(2) : "3306";
        info.database = m.group(3);
        return info;
    }

    private static class DbInfo {
        String host;
        String port;
        String database;
    }

    public record BackupResult(boolean success, String fileName, String message, long sizeKb) {
    }

    public record BackupFile(String name, long sizeKb, LocalDateTime createdAt) {
    }
}
