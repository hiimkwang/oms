package com.oms.module.gym.repository;

import com.oms.module.gym.entity.GymExercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GymExerciseRepository extends JpaRepository<GymExercise, Long> {
    Optional<GymExercise> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
