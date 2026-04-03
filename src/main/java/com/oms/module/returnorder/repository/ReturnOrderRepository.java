package com.oms.module.returnorder.repository;
import com.oms.module.returnorder.entity.ReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReturnOrderRepository extends JpaRepository<ReturnOrder, Long> {
    Optional<ReturnOrder> findByReturnCode(String returnCode);
}