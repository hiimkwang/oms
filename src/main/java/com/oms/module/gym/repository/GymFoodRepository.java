package com.oms.module.gym.repository;

import com.oms.module.gym.entity.GymFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GymFoodRepository extends JpaRepository<GymFood, Long> {
    Optional<GymFood> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
