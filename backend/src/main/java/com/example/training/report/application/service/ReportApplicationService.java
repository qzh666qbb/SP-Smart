package com.example.training.report.application.service;

import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ReportApplicationService.class);


    private final StudentRepository studentRepository;
    private final PhysicalTestRepository physicalTestRepository;
    private final JdbcTemplate jdbcTemplate;

    public Map<String, Object> generateAttendanceReport(Long classGroupId, LocalDate startDate, LocalDate endDate) {
        log.info("生成出勤报表: classGroupId={}, startDate={}, endDate={}", classGroupId, startDate, endDate);
        Map<String, Object> report = new HashMap<>();
        report.put("type", "ATTENDANCE");
        report.put("classGroupId", classGroupId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("generatedAt", LocalDateTime.now());

        List<Map<String, Object>> lessonSummaries = jdbcTemplate.queryForList(
                "SELECT l.id as lesson_id, l.lesson_date, l.start_time, l.end_time, l.status as lesson_status, " +
                        "l.course_id, c.course_name, l.teacher_id, u.username as teacher_name " +
                        "FROM lesson l " +
                        "LEFT JOIN course c ON l.course_id = c.id " +
                        "LEFT JOIN sys_user u ON l.teacher_id = u.id " +
                        "WHERE l.class_group_id = ? AND l.lesson_date BETWEEN ? AND ? AND l.deleted_at IS NULL " +
                        "ORDER BY l.lesson_date",
                classGroupId, startDate, endDate
        );
        report.put("lessons", lessonSummaries);

        List<Map<String, Object>> studentAttendanceDetails = jdbcTemplate.queryForList(
                "SELECT s.id as student_id, s.name as student_name, s.student_no, " +
                        "ls.lesson_id, ls.attendance_status, ls.sign_in_at, ls.consume_hours, " +
                        "ls.should_consume, ls.attendance_remark, ls.confirmed_at " +
                        "FROM lesson_student ls " +
                        "JOIN lesson l ON ls.lesson_id = l.id " +
                        "JOIN student s ON ls.student_id = s.id " +
                        "WHERE l.class_group_id = ? AND l.lesson_date BETWEEN ? AND ? " +
                        "AND s.deleted_at IS NULL " +
                        "ORDER BY s.id, l.lesson_date",
                classGroupId, startDate, endDate
        );
        report.put("studentAttendanceDetails", studentAttendanceDetails);

        Map<String, Integer> summary = new HashMap<>();
        int present = 0, absent = 0, late = 0, leave = 0, total = 0;
        for (Map<String, Object> row : studentAttendanceDetails) {
            String status = (String) row.get("attendance_status");
            total++;
            if ("PRESENT".equals(status)) present++;
            else if ("ABSENT".equals(status)) absent++;
            else if ("LATE".equals(status)) late++;
            else if ("LEAVE".equals(status)) leave++;
        }
        summary.put("totalRecords", total);
        summary.put("present", present);
        summary.put("absent", absent);
        summary.put("late", late);
        summary.put("leave", leave);
        summary.put("attendanceRate", total > 0 ? (int) Math.round(present * 100.0 / total) : 0);
        report.put("summary", summary);

        log.info("出勤报表生成完成: {} 条课程记录, {} 条出勤明细", lessonSummaries.size(), total);
        return report;
    }

    public Map<String, Object> generateHourReport(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("生成课时报表: studentId={}, startDate={}, endDate={}", studentId, startDate, endDate);
        Map<String, Object> report = new HashMap<>();
        report.put("type", "HOUR");
        report.put("studentId", studentId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("generatedAt", LocalDateTime.now());

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Map<String, Object> studentInfo = new HashMap<>();
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            studentInfo.put("id", s.getId());
            studentInfo.put("studentNo", s.getStudentNo());
            studentInfo.put("name", s.getName());
            studentInfo.put("gender", s.getGender());
            studentInfo.put("status", s.getStatus());
        }
        report.put("student", studentInfo);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Map<String, Object>> transactions = jdbcTemplate.queryForList(
                "SELECT ht.id, ht.transaction_no, ht.transaction_type, ht.change_amount, " +
                        "ht.before_balance, ht.after_balance, ht.hour_source, ht.reason, " +
                        "ht.lesson_id, ht.created_at, u.username as operator_name " +
                        "FROM hour_transaction ht " +
                        "LEFT JOIN sys_user u ON ht.operator_id = u.id " +
                        "WHERE ht.student_id = ? AND ht.created_at BETWEEN ? AND ? " +
                        "ORDER BY ht.created_at DESC",
                studentId, startDateTime, endDateTime
        );
        report.put("transactions", transactions);

        BigDecimal totalRecharge = BigDecimal.ZERO;
        BigDecimal totalConsume = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;
        for (Map<String, Object> tx : transactions) {
            String type = (String) tx.get("transaction_type");
            BigDecimal amount = (BigDecimal) tx.get("change_amount");
            if (amount == null) continue;
            if ("RECHARGE".equals(type)) totalRecharge = totalRecharge.add(amount);
            else if ("CONSUME".equals(type)) totalConsume = totalConsume.add(amount.abs());
            else if ("REFUND".equals(type)) totalRefund = totalRefund.add(amount.abs());
        }

        Map<String, Object> totals = new HashMap<>();
        totals.put("recharge", totalRecharge);
        totals.put("consume", totalConsume);
        totals.put("refund", totalRefund);
        report.put("totals", totals);

        Map<String, Object> accountInfo = jdbcTemplate.queryForList(
                "SELECT * FROM student_hour_account WHERE student_id = ? LIMIT 1",
                studentId
        ).stream().findFirst().orElse(new HashMap<>());
        report.put("accountCurrent", accountInfo);

        log.info("课时报表生成完成: {} 条交易记录", transactions.size());
        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generatePhysicalReport(Long studentId) {
        log.info("生成体测报表: studentId={}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        List<PhysicalTestRecord> records = physicalTestRepository.findByStudentId(studentId).stream()
                .sorted(Comparator.comparing(PhysicalTestRecord::getTestDate,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND);
        }

        PhysicalTestRecord latest = records.get(0);
        LocalDate reportDate = latest.getTestDate() == null ? LocalDate.now() : latest.getTestDate();
        List<ReportItem> latestItems = loadReportItems(latest.getId());
        double totalScore = calculateReportScore(latestItems);
        String totalLevel = levelOf(totalScore);

        Map<String, Object> classInfo = findStudentClass(studentId);
        List<Long> classStudentIds = findClassStudentIds(classInfo.get("id"));
        List<Long> gradeStudentIds = jdbcTemplate.queryForList(
                "SELECT id FROM student WHERE status = 'ACTIVE' AND deleted_at IS NULL", Long.class);

        Double previousScore = records.size() > 1
                ? calculateReportScore(loadReportItems(records.get(1).getId())) : null;
        double delta = previousScore == null ? 0 : totalScore - previousScore;

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basic_info", buildBasicInfo(student, classInfo, reportDate));
        root.put("summary_score", buildSummary(totalScore, totalLevel, studentId,
                classStudentIds, gradeStudentIds, previousScore, delta));
        root.put("seven_dimension", buildDimensions(latestItems));
        root.put("test_items", buildTestItems(latestItems));
        root.put("growth_trend", buildGrowthTrend(records));
        root.put("ai_profile", buildAiProfile(student.getName(), totalLevel, delta, buildDimensions(latestItems)));
        root.put("suggestions", Map.of(
                "sport", "每周进行3-5次运动，每次30-60分钟，以有氧运动为主，结合力量和协调训练。",
                "nutrition", "保证蛋白质、蔬果和奶制品摄入，多饮水，少吃油炸食品。",
                "sleep", "保证充足睡眠，5-6岁儿童每天睡眠10-12小时，规律作息。",
                "mental", "多鼓励孩子参与集体活动，培养运动兴趣和独立自主能力。"));
        root.put("warning", "温馨提示：本报告仅供参考，不能替代专业医疗诊断。如有健康问题，请咨询专业医生。");
        return root;
    }

    private Map<String, Object> buildBasicInfo(Student student, Map<String, Object> classInfo, LocalDate reportDate) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", student.getName());
        info.put("gender", "MALE".equals(student.getGender()) ? "男" : "FEMALE".equals(student.getGender()) ? "女" : "未知");
        info.put("class", classInfo.getOrDefault("class_name", "未分班"));
        info.put("age", formatAge(student.getBirthDate(), reportDate));
        info.put("birthday", student.getBirthDate());
        info.put("report_date", reportDate);
        return info;
    }

    private Map<String, Object> buildSummary(double score, String level, Long studentId,
                                              List<Long> classIds, List<Long> gradeIds,
                                              Double previousScore, double delta) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total_score", round(score));
        summary.put("level", level);
        summary.put("rank_class", rankOf(studentId, score, classIds));
        summary.put("rank_grade", rankOf(studentId, score, gradeIds));
        summary.put("eval_type", previousScore == null ? "首次测评" : "成长测评");
        summary.put("desc", previousScore == null
                ? "已建立首次体测成长基线"
                : String.format("较上次%s %.1f 分", delta >= 0 ? "提升" : "下降", Math.abs(delta)));
        return summary;
    }

    private List<Map<String, Object>> buildDimensions(List<ReportItem> items) {
        Map<String, ReportItem> byCode = items.stream().collect(Collectors.toMap(
                ReportItem::code, Function.identity(), (a, b) -> a));
        List<Map<String, Object>> dimensions = new ArrayList<>();
        dimensions.add(dimension("BMI", bmiScore(byCode), bmiLevel(byCode)));
        dimensions.add(dimension("协调能力", scoreOf(byCode, "BOTH_FEET_JUMP"), null));
        dimensions.add(dimension("平衡能力", scoreOf(byCode, "BALANCE_BEAM"), null));
        dimensions.add(dimension("灵敏性", scoreOf(byCode, "SHUTTLE_RUN"), null));
        dimensions.add(dimension("下肢力量", scoreOf(byCode, "STANDING_LONG_JUMP"), null));
        dimensions.add(dimension("上肢力量", scoreOf(byCode, "TENNIS_THROW"), null));
        dimensions.add(dimension("柔韧性", scoreOf(byCode, "SIT_AND_REACH"), null));
        return dimensions;
    }

    private List<Map<String, Object>> buildTestItems(List<ReportItem> items) {
        Map<String, ReportItem> byCode = items.stream().collect(Collectors.toMap(
                ReportItem::code, Function.identity(), (a, b) -> a));
        List<Map<String, Object>> result = new ArrayList<>();
        addTestItem(result, byCode.get("HEIGHT"));
        double bmi = bmiValue(byCode);
        if (bmi > 0) result.add(testItem("BMI", formatNumber(bmi), bmiScore(byCode), bmiLevel(byCode)));
        for (String code : List.of("SIT_AND_REACH", "STANDING_LONG_JUMP", "BOTH_FEET_JUMP",
                "TENNIS_THROW", "SHUTTLE_RUN", "BALANCE_BEAM")) {
            addTestItem(result, byCode.get(code));
        }
        Set<String> shown = Set.of("HEIGHT", "WEIGHT", "SIT_AND_REACH", "STANDING_LONG_JUMP",
                "BOTH_FEET_JUMP", "TENNIS_THROW", "SHUTTLE_RUN", "BALANCE_BEAM");
        items.stream().filter(item -> !shown.contains(item.code())).forEach(item -> addTestItem(result, item));
        return result;
    }

    private Map<String, Object> buildGrowthTrend(List<PhysicalTestRecord> records) {
        List<PhysicalTestRecord> chronological = records.stream()
                .sorted(Comparator.comparing(PhysicalTestRecord::getTestDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        PhysicalTestRecord base = chronological.get(0);
        List<ReportItem> baseItems = loadReportItems(base.getId());
        List<Map<String, Object>> baseData = buildTestItems(baseItems).stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("item_name", item.get("item_name"));
            row.put("base_value", item.get("value"));
            return row;
        }).toList();
        List<Map<String, Object>> scorePoints = chronological.stream().map(record -> {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", record.getTestDate());
            point.put("score", round(calculateReportScore(loadReportItems(record.getId()))));
            return point;
        }).toList();
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("base_score", round(calculateReportScore(baseItems)));
        trend.put("tip", chronological.size() == 1
                ? "本次为首次测评，系统已建立成长基线；第二次体测导入后将自动生成趋势分析。"
                : "已根据历次体测生成成长趋势，可持续关注优势和待提升项。");
        trend.put("base_data", baseData);
        trend.put("score_points", scorePoints);
        return trend;
    }

    private Map<String, Object> buildAiProfile(String name, String overallLevel, double delta,
                                                List<Map<String, Object>> dimensions) {
        List<Map<String, Object>> sorted = dimensions.stream()
                .sorted(Comparator.comparingDouble(row -> -number(row.get("score")))).toList();
        List<String> advantages = sorted.stream().filter(row -> number(row.get("score")) >= 80).limit(4)
                .map(row -> row.get("name") + "表现" + row.get("level")).toList();
        List<String> focuses = dimensions.stream().filter(row -> number(row.get("score")) < 80)
                .sorted(Comparator.comparingDouble(row -> number(row.get("score")))).limit(3)
                .map(row -> row.get("name") + "可进一步加强").toList();
        String progress = delta > 0.05 ? "，较上次有所提升" : delta < -0.05 ? "，较上次有所回落" : "";
        String strong = advantages.isEmpty() ? "尚无明显优势项" : String.join("、", advantages);
        String focus = focuses.isEmpty() ? "各项能力较为均衡" : String.join("、", focuses);
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("summary", name + "本次综合表现" + overallLevel + progress + "。" + strong + "；" + focus + "。");
        profile.put("advantage_list", advantages);
        profile.put("focus_list", focuses);
        return profile;
    }

    private List<ReportItem> loadReportItems(Long recordId) {
        return physicalTestRepository.findByRecordId(recordId).stream().map(detail -> {
            PhysicalTestItem item = physicalTestRepository.findItemById(detail.getItemId()).orElse(null);
            String code = item == null ? "ITEM_" + detail.getItemId() : item.getItemCode();
            String name = item == null ? "未知指标" : item.getItemName();
            String unit = item == null ? "" : item.getUnit();
            double score = normalizeScore(detail.getConvertedScore());
            return new ReportItem(code, name, unit, detail.getRawValue(), score,
                    translateLevel(detail.getLevel(), score));
        }).toList();
    }

    private Map<String, Object> findStudentClass(Long studentId) {
        return jdbcTemplate.queryForList("SELECT cg.id, cg.class_name FROM class_group_student cgs " +
                        "JOIN class_group cg ON cg.id = cgs.class_group_id " +
                        "WHERE cgs.student_id = ? AND cgs.status = 'ACTIVE' AND cgs.deleted_at IS NULL " +
                        "AND cg.deleted_at IS NULL ORDER BY cgs.joined_at DESC LIMIT 1", studentId)
                .stream().findFirst().orElseGet(LinkedHashMap::new);
    }

    private List<Long> findClassStudentIds(Object classId) {
        if (!(classId instanceof Number number)) return List.of();
        return jdbcTemplate.queryForList("SELECT student_id FROM class_group_student " +
                "WHERE class_group_id = ? AND status = 'ACTIVE' AND deleted_at IS NULL", Long.class, number.longValue());
    }

    private String rankOf(Long studentId, double currentScore, List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) return "-";
        int higher = 0;
        int tested = 0;
        for (Long id : studentIds) {
            Optional<PhysicalTestRecord> record = physicalTestRepository.findLatestByStudentId(id);
            if (record.isEmpty()) continue;
            tested++;
            double score = calculateReportScore(loadReportItems(record.get().getId()));
            if (!id.equals(studentId) && score > currentScore) higher++;
        }
        return tested == 0 ? "-" : (higher + 1) + "/" + tested;
    }

    private double calculateReportScore(List<ReportItem> items) {
        List<Double> dimensionScores = buildDimensions(items).stream()
                .map(row -> number(row.get("score")))
                .filter(score -> score > 0)
                .toList();
        if (!dimensionScores.isEmpty()) {
            return dimensionScores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        }
        return items.stream().mapToDouble(ReportItem::score).filter(score -> score > 0).average().orElse(0);
    }

    private static Map<String, Object> dimension(String name, double score, String forcedLevel) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("score", round(score));
        row.put("level", forcedLevel == null ? levelOf(score) : forcedLevel);
        return row;
    }

    private static Map<String, Object> testItem(String name, String value, double score, String level) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("item_name", name);
        row.put("value", value);
        row.put("score", round(score));
        row.put("level", level);
        return row;
    }

    private static void addTestItem(List<Map<String, Object>> result, ReportItem item) {
        if (item == null) return;
        String value = formatNumber(item.rawValue() == null ? 0 : item.rawValue().doubleValue()) + item.unit();
        result.add(testItem(item.name(), value, item.score(), item.level()));
    }

    private static double scoreOf(Map<String, ReportItem> byCode, String code) {
        return byCode.containsKey(code) ? byCode.get(code).score() : 0;
    }

    private static double bmiValue(Map<String, ReportItem> byCode) {
        ReportItem height = byCode.get("HEIGHT");
        ReportItem weight = byCode.get("WEIGHT");
        if (height == null || weight == null || height.rawValue() == null || weight.rawValue() == null) return 0;
        double metres = height.rawValue().doubleValue() / 100d;
        return metres <= 0 ? 0 : weight.rawValue().doubleValue() / (metres * metres);
    }

    private static double bmiScore(Map<String, ReportItem> byCode) {
        double bmi = bmiValue(byCode);
        if (bmi <= 0) return 0;
        if (bmi >= 14 && bmi <= 18) return 100;
        if (bmi >= 13 && bmi <= 19) return 80;
        if (bmi >= 12 && bmi <= 21) return 60;
        return 40;
    }

    private static String bmiLevel(Map<String, ReportItem> byCode) {
        double bmi = bmiValue(byCode);
        if (bmi <= 0) return "暂无数据";
        if (bmi >= 14 && bmi <= 18) return "正常";
        return bmi < 14 ? "偏瘦" : "偏高";
    }

    private static double normalizeScore(BigDecimal score) {
        if (score == null) return 0;
        double value = score.doubleValue();
        return value <= 5 ? value * 20 : Math.min(value, 100);
    }

    private static String translateLevel(String level, double score) {
        if (level == null || level.isBlank()) return levelOf(score);
        return switch (level) {
            case "EXCELLENT" -> "优秀";
            case "GOOD" -> "良好";
            case "PASS" -> "合格";
            case "INADEQUATE" -> "待提升";
            case "TERRIBLE", "FAIL" -> "不合格";
            default -> level;
        };
    }

    private static String levelOf(double score) {
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 60) return "合格";
        return "待提升";
    }

    private static String formatAge(LocalDate birthDate, LocalDate reportDate) {
        if (birthDate == null) return "未知";
        Period period = Period.between(birthDate, reportDate);
        return period.getYears() + "岁" + period.getMonths() + "个月";
    }

    private static double round(double value) {
        return Math.round(value * 10d) / 10d;
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
    }

    private static double number(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0;
    }

    private record ReportItem(String code, String name, String unit, BigDecimal rawValue, double score, String level) {}
}
