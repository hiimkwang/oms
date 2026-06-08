package com.oms.module.gym.repository;

import com.oms.module.gym.entity.GymWorkoutDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GymWorkoutDayRepository extends JpaRepository<GymWorkoutDay, Long> {
    List<GymWorkoutDay> findAllByOrderByDayOfWeekAsc();

    Optional<GymWorkoutDay> findByDayOfWeek(int dayOfWeek);
}
