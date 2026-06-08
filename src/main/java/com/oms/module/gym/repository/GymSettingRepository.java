package com.oms.module.gym.repository;

import com.oms.module.gym.entity.GymSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymSettingRepository extends JpaRepository<GymSetting, Long> {
}
