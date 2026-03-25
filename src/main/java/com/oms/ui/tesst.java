package com.oms.ui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class tesst {
    public static void main(String[] args) {
        String inputPath = "Y:\\2. Software Team\\Tai lieu phan mem\\UPNOTEBOOKLM\\UP_FILE";
        String csvPath = "Y:\\2. Software Team\\Tai lieu phan mem\\UPNOTEBOOKLM\\UP_FILE\\danh_sach_file.csv"; // File kết quả sẽ nằm ngay trong folder up

        File folder = new File(inputPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Thư mục không tồn tại.");
            return;
        }

        File[] fileList = folder.listFiles();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Sử dụng try-with-resources để tự động đóng file sau khi ghi xong
        try (PrintWriter writer = new PrintWriter(new File(csvPath))) {

            // Ghi Header (Tiêu đề cột) - Thêm BOM để Excel nhận diện được tiếng Việt/UTF-8
            writer.write('\ufeff');
            writer.println("Tên File,Ngày Sửa Đổi,Định Dạng");

            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        String dateModified = sdf.format(file.lastModified());

                        // Lấy định dạng file
                        String extension = "";
                        int i = fileName.lastIndexOf('.');
                        if (i > 0) {
                            extension = fileName.substring(i + 1).toUpperCase();
                        } else {
                            extension = "N/A";
                        }

                        // Format dòng CSV: Bao tên file trong dấu ngoặc kép đề phòng tên file có dấu phẩy
                        writer.printf("\"%s\",%s,%s%n", fileName, dateModified, extension);
                    }
                }
            }

            System.out.println("Đã xuất file CSV thành công tại: " + csvPath);

        } catch (IOException e) {
            System.err.println("Có lỗi khi ghi file: " + e.getMessage());
        }
    }
}