package com.oms.module.setting.repository;

import com.oms.module.setting.entity.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {

    // Lấy danh sách theo Loại (dùng cho giao diện dropdown)
    List<MasterData> findByDataTypeOrderBySortOrderAsc(String dataType);

    // Kiểm tra xem dữ liệu đã tồn tại chưa (Không phân biệt hoa thường)
    boolean existsByDataTypeAndDataValueIgnoreCase(String dataType, String dataValue);

    // Tìm kiếm chính xác 1 bản ghi
    Optional<MasterData> findByDataTypeAndDataValueIgnoreCase(String dataType, String dataValue);
}