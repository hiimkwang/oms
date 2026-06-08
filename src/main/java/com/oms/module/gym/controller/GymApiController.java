package com.oms.module.gym.controller;

import com.oms.module.gym.dto.DiaryDayResponse;
import com.oms.module.gym.entity.GymDailyLog;
import com.oms.module.gym.entity.GymDiaryEntry;
import com.oms.module.gym.entity.GymExercise;
import com.oms.module.gym.entity.GymFood;
import com.oms.module.gym.entity.GymSetting;
import com.oms.module.gym.entity.GymWorkoutDay;
import com.oms.module.gym.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * API cho trang quản lý calo & gym. Đặt riêng dưới /api/gym/** (không yêu cầu đăng nhập,
 * xem cấu hình SecurityConfig). Hoàn toàn tách biệt với nghiệp vụ OMS.
 */
@RestController
@RequestMapping("/api/gym")
@RequiredArgsConstructor
public class GymApiController {

    private final GymService gymService;

    // ---------- FOOD DB ----------
    @GetMapping("/foods")
    public List<GymFood> getFoods() {
        return gymService.getAllFoods();
    }

    @PostMapping("/foods")
    public GymFood saveFood(@RequestBody GymFood food) {
        return gymService.saveFood(food);
    }

    @DeleteMapping("/foods/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable Long id) {
        gymService.deleteFood(id);
        return ResponseEntity.ok().build();
    }

    // ---------- DIARY ----------
    @GetMapping("/diary")
    public DiaryDayResponse getDiary(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return gymService.getDiaryDay(date);
    }

    @PostMapping("/diary")
    public GymDiaryEntry addEntry(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestBody GymDiaryEntry entry) {
        return gymService.addDiaryEntry(date, entry);
    }

    @PutMapping("/diary/entry/{id}")
    public GymDiaryEntry updateEntryGrams(@PathVariable Long id, @RequestBody Map<String, Double> body) {
        return gymService.updateEntryGrams(id, body.getOrDefault("grams", 0.0));
    }

    @DeleteMapping("/diary/entry/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        gymService.deleteDiaryEntry(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/diary/daily")
    public GymDailyLog upsertDaily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestBody GymDailyLog daily) {
        return gymService.upsertDailyLog(date, daily);
    }

    /** Sao chép món ăn từ ngày 'from' sang ngày 'to'. */
    @PostMapping("/diary/copy")
    public ResponseEntity<?> copyDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        int n = gymService.copyDay(from, to);
        return ResponseEntity.ok(Map.of("copied", n));
    }

    // ---------- RESET / XÓA DỮ LIỆU ----------
    /** Xóa hết món + thông tin tổng hợp của 1 ngày. */
    @DeleteMapping("/diary/day")
    public ResponseEntity<?> clearDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        gymService.clearDay(date);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings/reset")
    public GymSetting resetSettings() {
        return gymService.resetSettings();
    }

    @PostMapping("/reset/history")
    public ResponseEntity<?> clearHistory() {
        gymService.clearHistory();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset/all")
    public ResponseEntity<?> resetAll() {
        gymService.resetAll();
        return ResponseEntity.ok().build();
    }

    // ---------- EXERCISE LIBRARY ----------
    @GetMapping("/exercises")
    public List<GymExercise> getExercises() {
        return gymService.getAllExercises();
    }

    @PostMapping("/exercises")
    public GymExercise saveExercise(@RequestBody GymExercise ex) {
        return gymService.saveExercise(ex);
    }

    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long id) {
        gymService.deleteExercise(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(Map.of("url", gymService.uploadImage(file)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---------- WORKOUT ----------
    @GetMapping("/workout")
    public List<GymWorkoutDay> getWorkout() {
        return gymService.getWorkout();
    }

    @PutMapping("/workout")
    public List<GymWorkoutDay> saveWorkout(@RequestBody List<GymWorkoutDay> days) {
        return gymService.saveWorkout(days);
    }

    // ---------- SETTING ----------
    @GetMapping("/settings")
    public GymSetting getSetting() {
        return gymService.getSetting();
    }

    @PutMapping("/settings")
    public GymSetting saveSetting(@RequestBody GymSetting setting) {
        return gymService.saveSetting(setting);
    }

    // ---------- STATS ----------
    @GetMapping("/stats")
    public Map<String, Object> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return gymService.getStats(from, to);
    }
}
