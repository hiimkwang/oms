package com.oms.module.setting.repository;

import com.oms.module.receipt.entity.Receipt;
import com.oms.module.setting.entity.SalesChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesChannelRepository extends JpaRepository<SalesChannel, Long> {

}
