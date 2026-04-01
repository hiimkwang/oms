package com.oms.module.order.repository;

import com.oms.module.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    Optional<OrderDetail> findBySerialNumber(String serialNumber);
}