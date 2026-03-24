package com.oms.module.setting.service;

import com.oms.module.setting.entity.MasterData;
import com.oms.module.setting.repository.MasterDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final MasterDataRepository masterDataRepository;

    // ==========================================
    // CÁC HÀM PHỤC VỤ GIAO DIỆN BÁN HÀNG / KHO
    // ==========================================

    /**
     * Lấy danh sách giá trị (String) để đổ vào các ô Dropdown (Select)
     */
    public List<String> getValuesByType(String dataType) {
        return masterDataRepository.findByDataTypeOrderBySortOrderAsc(dataType)
                .stream()
                .map(MasterData::getDataValue)
                .collect(Collectors.toList());
    }

    /**
     * Thêm nhanh một danh mục mới nếu chưa tồn tại (Dùng cho nút "+" ngoài giao diện)
     */
    @Transactional
    public MasterData createIfNotExist(String dataType, String dataValue) {
        // Kiểm tra xem đã có chưa (VD: Đã có 'Aula' thì không thêm 'aula' nữa)
        if (masterDataRepository.existsByDataTypeAndDataValueIgnoreCase(dataType, dataValue)) {
            return masterDataRepository.findByDataTypeAndDataValueIgnoreCase(dataType, dataValue).get();
        }

        // Tìm thứ tự (sortOrder) lớn nhất hiện tại để xếp cái mới xuống cuối cùng
        List<MasterData> existingList = masterDataRepository.findByDataTypeOrderBySortOrderAsc(dataType);
        int nextSortOrder = existingList.isEmpty() ? 1 : existingList.get(existingList.size() - 1).getSortOrder() + 1;

        MasterData newData = MasterData.builder()
                .dataType(dataType.toUpperCase()) // Chuẩn hóa Type luôn viết hoa (VD: BRAND)
                .dataValue(dataValue.trim())      // Cắt khoảng trắng 2 đầu
                .sortOrder(nextSortOrder)
                .build();

        return masterDataRepository.save(newData);
    }

    // ==========================================
    // CÁC HÀM PHỤC VỤ MÀN HÌNH "CÀI ĐẶT HỆ THỐNG"
    // ==========================================

    /**
     * Lấy toàn bộ Entity để hiển thị lên bảng Quản lý Danh mục
     */
    public List<MasterData> getAllByType(String dataType) {
        return masterDataRepository.findByDataTypeOrderBySortOrderAsc(dataType);
    }

    /**
     * Lấy chi tiết 1 bản ghi bằng ID
     */
    public MasterData getById(Long id) {
        return masterDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu cấu hình với ID: " + id));
    }

    /**
     * Sửa tên hoặc thứ tự hiển thị của một danh mục
     */
    @Transactional
    public MasterData update(Long id, MasterData request) {
        MasterData existingData = getById(id);

        // Nếu người dùng đổi tên, phải kiểm tra xem tên mới có bị trùng với cái khác không
        if (!existingData.getDataValue().equalsIgnoreCase(request.getDataValue().trim()) &&
                masterDataRepository.existsByDataTypeAndDataValueIgnoreCase(existingData.getDataType(), request.getDataValue().trim())) {
            throw new RuntimeException("Giá trị này đã tồn tại trong hệ thống!");
        }

        existingData.setDataValue(request.getDataValue().trim());

        if (request.getSortOrder() != null) {
            existingData.setSortOrder(request.getSortOrder());
        }

        return masterDataRepository.save(existingData);
    }

    /**
     * Xóa danh mục
     */
    @Transactional
    public void delete(Long id) {
        MasterData existingData = getById(id);

        // TODO: Trong tương lai, bạn có thể thêm logic kiểm tra xem Danh mục này
        // đã được dùng trong bảng Product chưa. Nếu dùng rồi thì không cho xóa (ném Exception),
        // chỉ cho phép "Ẩn" đi để tránh lỗi mất dữ liệu báo cáo cũ.

        masterDataRepository.delete(existingData);
    }
}