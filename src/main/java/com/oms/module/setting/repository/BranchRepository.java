package com.oms.module.setting.repository;

import com.oms.module.setting.entity.Branch;
import com.oms.module.setting.entity.SalesChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

}
