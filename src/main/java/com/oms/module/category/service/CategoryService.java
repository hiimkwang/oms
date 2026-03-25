package com.oms.module.category.service;

import com.oms.module.category.entity.Category;
import com.oms.module.category.repository.CategoryRepository;
import com.oms.module.media.service.FileService;
import com.oms.module.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository; // Bổ sung dòng này
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không thấy danh mục"));
    }

    private final FileService fileService; // Inject FileService vào

    @Transactional
    public Category create(String name, String description, MultipartFile image) {
        // Gọi FileService để lấy link ảnh
        String imageUrl = fileService.uploadFile(image);
        Category category = Category.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl) // Lưu link public (https://oms...) vào DB
                .build();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, String name, String description, boolean isImageRemoved, MultipartFile image) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(name);
        category.setDescription(description);

        if (image != null && !image.isEmpty()) {
            // Upload ảnh mới
            category.setImageUrl(fileService.uploadFile(image));
        } else if (isImageRemoved) {
            // Xóa hẳn ảnh cũ
            category.setImageUrl(null);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        // 1. Chuyển các sản phẩm thuộc danh mục này về "Chưa phân loại" (null)
        productRepository.setCategoryNullByCategoryId(id);

        // 2. Xóa danh mục
        categoryRepository.deleteById(id);
    }
    @Transactional
    public void bulkDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        for (Long id : ids) {
            // Tận dụng lại hàm xóa đơn lẻ ở trên để xử lý an toàn
            delete(id);
        }
    }

}