package com.oms.module.gym.repository;

import com.oms.module.gym.entity.GymDiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GymDiaryEntryRepository extends JpaRepository<GymDiaryEntry, Long> {
    List<GymDiaryEntry> findByLogDateOrderByIdAsc(LocalDate logDate);

    List<GymDiaryEntry> findByLogDateBetweenOrderByLogDateAsc(LocalDate from, LocalDate to);
}
