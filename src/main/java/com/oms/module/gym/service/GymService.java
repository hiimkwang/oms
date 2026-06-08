package com.oms.module.gym.service;

import com.oms.module.gym.dto.DiaryDayResponse;
import com.oms.module.gym.entity.*;
import com.oms.module.gym.repository.*;
import com.oms.module.media.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GymService {

    private final GymFoodRepository foodRepository;
    private final GymDiaryEntryRepository diaryRepository;
    private final GymDailyLogRepository dailyRepository;
    private final GymWorkoutDayRepository workoutRepository;
    private final GymSettingRepository settingRepository;
    private final GymExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper;
    private final FileService fileService;

    // ====================== FOOD DATABASE ======================
    public List<GymFood> getAllFoods() {
        return foodRepository.findAll(org.springframework.data.domain.Sort.by("name"));
    }

    @Transactional
    public GymFood saveFood(GymFood food) {
        if (food.getName() != null) {
            food.setName(food.getName().trim());
        }
        // Nếu là món mới nhưng trùng tên (không phân biệt hoa thường) -> cập nhật món cũ
        if (food.getId() == null && food.getName() != null) {
            return foodRepository.findByNameIgnoreCase(food.getName())
                    .map(existing -> {
                        existing.setCarb(food.getCarb());
                        existing.setFat(food.getFat());
                        existing.setProtein(food.getProtein());
                        existing.setCalo(food.getCalo());
                        return foodRepository.save(existing);
                    })
                    .orElseGet(() -> foodRepository.save(food));
        }
        return foodRepository.save(food);
    }

    @Transactional
    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }

    // ====================== THƯ VIỆN BÀI TẬP ======================
    public List<GymExercise> getAllExercises() {
        return exerciseRepository.findAll(org.springframework.data.domain.Sort.by("name"));
    }

    @Transactional
    public GymExercise saveExercise(GymExercise ex) {
        if (ex.getName() != null) {
            ex.setName(ex.getName().trim());
        }
        if (ex.getId() == null && ex.getName() != null) {
            return exerciseRepository.findByNameIgnoreCase(ex.getName())
                    .map(existing -> {
                        existing.setImageUrl(ex.getImageUrl());
                        existing.setMuscleGroup(ex.getMuscleGroup());
                        existing.setNote(ex.getNote());
                        return exerciseRepository.save(existing);
                    })
                    .orElseGet(() -> exerciseRepository.save(ex));
        }
        return exerciseRepository.save(ex);
    }

    @Transactional
    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }

    /** Upload ảnh minh họa (tái sử dụng FileService của OMS, chỉ nhận file ảnh ≤ 5MB). */
    public String uploadImage(MultipartFile file) {
        return fileService.uploadFile(file);
    }

    // ====================== DIARY (THEO NGÀY) ======================
    public DiaryDayResponse getDiaryDay(LocalDate date) {
        List<GymDiaryEntry> entries = diaryRepository.findByLogDateOrderByIdAsc(date);
        GymDailyLog daily = dailyRepository.findByLogDate(date).orElse(null);

        double tc = 0, tf = 0, tp = 0, tcal = 0;
        for (GymDiaryEntry e : entries) {
            tc += e.getCarb();
            tf += e.getFat();
            tp += e.getProtein();
            tcal += e.getCalo();
        }
        return new DiaryDayResponse(date.toString(), daily, entries,
                round(tc), round(tf), round(tp), round(tcal));
    }

    @Transactional
    public GymDiaryEntry addDiaryEntry(LocalDate date, GymDiaryEntry entry) {
        entry.setId(null);
        entry.setLogDate(date);
        return diaryRepository.save(entry);
    }

    /** Cập nhật số gram của 1 món đã thêm, macro được tính lại theo tỉ lệ. */
    @Transactional
    public GymDiaryEntry updateEntryGrams(Long id, double newGrams) {
        GymDiaryEntry e = diaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món"));
        double oldGrams = e.getGrams();
        if (oldGrams > 0) {
            double ratio = newGrams / oldGrams;
            e.setCarb(round(e.getCarb() * ratio));
            e.setFat(round(e.getFat() * ratio));
            e.setProtein(round(e.getProtein() * ratio));
            e.setCalo(round(e.getCalo() * ratio));
        }
        e.setGrams(newGrams);
        return diaryRepository.save(e);
    }

    @Transactional
    public void deleteDiaryEntry(Long id) {
        diaryRepository.deleteById(id);
    }

    @Transactional
    public GymDailyLog upsertDailyLog(LocalDate date, GymDailyLog incoming) {
        GymDailyLog log = dailyRepository.findByLogDate(date).orElseGet(GymDailyLog::new);
        log.setLogDate(date);
        log.setDayType(incoming.getDayType());
        log.setWeight(incoming.getWeight());
        log.setNote(incoming.getNote());
        return dailyRepository.save(log);
    }

    /** Sao chép toàn bộ món ăn từ ngày nguồn sang ngày đích (cộng dồn vào ngày đích). */
    @Transactional
    public int copyDay(LocalDate from, LocalDate to) {
        List<GymDiaryEntry> source = diaryRepository.findByLogDateOrderByIdAsc(from);
        for (GymDiaryEntry s : source) {
            GymDiaryEntry e = new GymDiaryEntry();
            e.setLogDate(to);
            e.setFoodName(s.getFoodName());
            e.setGrams(s.getGrams());
            e.setCarb(s.getCarb());
            e.setFat(s.getFat());
            e.setProtein(s.getProtein());
            e.setCalo(s.getCalo());
            diaryRepository.save(e);
        }
        return source.size();
    }

    // ====================== LỊCH TẬP ======================
    public List<GymWorkoutDay> getWorkout() {
        return workoutRepository.findAllByOrderByDayOfWeekAsc();
    }

    @Transactional
    public List<GymWorkoutDay> saveWorkout(List<GymWorkoutDay> days) {
        for (GymWorkoutDay incoming : days) {
            GymWorkoutDay day = workoutRepository.findByDayOfWeek(incoming.getDayOfWeek())
                    .orElseGet(GymWorkoutDay::new);
            day.setDayOfWeek(incoming.getDayOfWeek());
            day.setProgram(incoming.getProgram());
            day.setExercises(incoming.getExercises());
            workoutRepository.save(day);
        }
        return getWorkout();
    }

    // ====================== SETTING ======================
    public GymSetting getSetting() {
        return settingRepository.findById(1L).orElseGet(() -> settingRepository.save(new GymSetting()));
    }

    @Transactional
    public GymSetting saveSetting(GymSetting incoming) {
        incoming.setId(1L);
        return settingRepository.save(incoming);
    }

    // ====================== THỐNG KÊ ======================
    public Map<String, Object> getStats(LocalDate from, LocalDate to) {
        List<Map<String, Object>> weightSeries = new ArrayList<>();
        for (GymDailyLog log : dailyRepository.findByLogDateBetweenAndWeightIsNotNullOrderByLogDateAsc(from, to)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", log.getLogDate().toString());
            m.put("weight", log.getWeight());
            weightSeries.add(m);
        }

        Map<LocalDate, double[]> byDate = new LinkedHashMap<>(); // [calo, carb, fat, protein]
        for (GymDiaryEntry e : diaryRepository.findByLogDateBetweenOrderByLogDateAsc(from, to)) {
            double[] agg = byDate.computeIfAbsent(e.getLogDate(), k -> new double[4]);
            agg[0] += e.getCalo();
            agg[1] += e.getCarb();
            agg[2] += e.getFat();
            agg[3] += e.getProtein();
        }
        List<Map<String, Object>> calorieSeries = new ArrayList<>();
        byDate.forEach((d, agg) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", d.toString());
            m.put("calo", round(agg[0]));
            m.put("carb", round(agg[1]));
            m.put("fat", round(agg[2]));
            m.put("protein", round(agg[3]));
            calorieSeries.add(m);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("from", from.toString());
        result.put("to", to.toString());
        result.put("weightSeries", weightSeries);
        result.put("calorieSeries", calorieSeries);
        return result;
    }

    // ====================== RESET / XÓA DỮ LIỆU ======================
    /** Xóa hết món ăn + thông tin tổng hợp của 1 ngày (đưa bộ đếm ngày đó về 0). */
    @Transactional
    public void clearDay(LocalDate date) {
        diaryRepository.deleteAll(diaryRepository.findByLogDateOrderByIdAsc(date));
        dailyRepository.findByLogDate(date).ifPresent(dailyRepository::delete);
    }

    /** Đưa cấu hình mục tiêu & chỉ số cơ thể về mặc định. */
    @Transactional
    public GymSetting resetSettings() {
        settingRepository.deleteById(1L);
        return settingRepository.save(new GymSetting());
    }

    /** Xóa toàn bộ lịch sử (nhật ký ăn uống + cân nặng theo ngày). Giữ DB món ăn, lịch tập, cấu hình. */
    @Transactional
    public void clearHistory() {
        diaryRepository.deleteAllInBatch();
        dailyRepository.deleteAllInBatch();
    }

    /** Xóa sạch toàn bộ dữ liệu gym và nạp lại dữ liệu mặc định. */
    @Transactional
    public void resetAll() {
        diaryRepository.deleteAllInBatch();
        dailyRepository.deleteAllInBatch();
        workoutRepository.deleteAllInBatch();
        exerciseRepository.deleteAllInBatch();
        foodRepository.deleteAllInBatch();
        settingRepository.deleteAll();
        seedDefaults(true);
    }

    // ====================== SEED DỮ LIỆU MẶC ĐỊNH ======================
    /**
     * Nạp dữ liệu mặc định. force=false: chỉ nạp khi bảng trống (dùng khi khởi động).
     * force=true: dữ liệu đã được xóa trước đó (dùng cho reset toàn bộ).
     */
    @Transactional
    public void seedDefaults(boolean force) {
        if (force || foodRepository.count() == 0) {
            seedFoods();
        }
        if (force || workoutRepository.count() == 0) {
            seedWorkout();
        }
        if (force || exerciseRepository.count() == 0) {
            seedExercises();
        }
        if (force || settingRepository.count() == 0) {
            settingRepository.save(new GymSetting());
        }
    }

    private List<GymFood> readSeedFoods() {
        List<GymFood> list = new ArrayList<>();
        try (InputStream is = new ClassPathResource("data/gym-foods.json").getInputStream()) {
            JsonNode arr = objectMapper.readTree(is);
            for (JsonNode n : arr) {
                GymFood f = new GymFood();
                f.setName(n.get("name").asText().trim());
                f.setCarb(n.get("carb").asDouble());
                f.setFat(n.get("fat").asDouble());
                f.setProtein(n.get("protein").asDouble());
                f.setCalo(n.get("calo").asDouble());
                if (n.has("portions") && !n.get("portions").isNull()) {
                    f.setPortions(objectMapper.writeValueAsString(n.get("portions")));
                }
                list.add(f);
            }
        } catch (Exception e) {
            log.error("❌ Lỗi đọc file món ăn gym: {}", e.getMessage());
        }
        return list;
    }

    private void seedFoods() {
        List<GymFood> foods = readSeedFoods();
        foodRepository.saveAll(foods);
        log.info("✅ Đã nạp {} món ăn vào database gym!", foods.size());
    }

    /**
     * Bổ sung gợi ý khẩu phần cho các món đã có sẵn trong DB (khi nâng cấp,
     * food đã được seed trước đó mà chưa có cột portions). Idempotent.
     */
    @Transactional
    public void backfillFoodPortions() {
        for (GymFood seed : readSeedFoods()) {
            if (seed.getPortions() == null) continue;
            foodRepository.findByNameIgnoreCase(seed.getName()).ifPresent(existing -> {
                if (existing.getPortions() == null || existing.getPortions().isBlank()) {
                    existing.setPortions(seed.getPortions());
                    foodRepository.save(existing);
                }
            });
        }
    }

    private void seedExercises() {
        String[][] lib = {
                {"Dumbbell bench press", "Ngực"}, {"Incline Barbell Bench Press", "Ngực"},
                {"Seated Chest Press", "Ngực"}, {"Pec Fly", "Ngực"}, {"Ab Crunch Machine", "Bụng"},
                {"Lat Pulldown", "Lưng"}, {"Wide Grip Cable Row", "Lưng"}, {"Tbar Row", "Lưng"},
                {"Rear Delt Fly", "Vai"}, {"The Overhand Bicep Curl", "Tay trước"},
                {"Leg Press", "Chân"}, {"Leg Extension", "Chân"}, {"Squat", "Chân"},
                {"Sumo deadlifts", "Chân"}, {"Steep walk", "Cardio"},
        };
        for (String[] e : lib) {
            GymExercise ex = new GymExercise();
            ex.setName(e[0]);
            ex.setMuscleGroup(e[1]);
            exerciseRepository.save(ex);
        }
        log.info("✅ Đã nạp {} bài tập mẫu vào thư viện gym!", lib.length);
    }

    private void seedWorkout() {
        String push = String.join("\n",
                "Dumbbell bench press", "Incline Barbell Bench Press",
                "Seated Chest Press", "Pec Fly", "Ab Crunch Machine");
        String pull = String.join("\n",
                "Lat Pulldown", "Wide Grip Cable Row", "Tbar Row",
                "Rear Delt Fly", "The Overhand Bicep Curl");
        String leg = String.join("\n",
                "Leg Press", "Leg Extension", "Squat", "Sumo deadlifts", "Steep walk");

        String[][] plan = {
                {"1", "Push", push}, {"2", "Pull", pull}, {"3", "Leg", leg},
                {"4", "Push", push}, {"5", "Pull", pull}, {"6", "Leg", leg},
                {"7", "Rest", ""},
        };
        for (String[] p : plan) {
            GymWorkoutDay d = new GymWorkoutDay();
            d.setDayOfWeek(Integer.parseInt(p[0]));
            d.setProgram(p[1]);
            d.setExercises(p[2]);
            workoutRepository.save(d);
        }
        log.info("✅ Đã nạp lịch tập PPL mẫu cho module gym!");
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
