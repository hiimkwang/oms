package com.oms.module.customer.repository;

import com.oms.module.customer.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
    Optional<CustomerGroup> findByCode(String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM CustomerGroup g WHERE g.code IN :codes")
    void deleteAllByCodeIn(@Param("codes") List<String> codes);
}