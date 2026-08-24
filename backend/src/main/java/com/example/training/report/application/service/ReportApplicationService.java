package com.example.training.report.application.service;

import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Map<String, Object> studentInfo = new HashMap<>();
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
            studentInfo.put("id", s.getId());
            studentInfo.put("studentNo", s.getStudentNo());
            studentInfo.put("name", s.getName());
            studentInfo.put("gender", s.getGender());
            studentInfo.put("birthDate", s.getBirthDate());
            studentInfo.put("status", s.getStatus());
            studentInfo.put("enrollmentDate", s.getEnrollmentDate());
            studentInfo.put("primaryTeacherId", s.getPrimaryTeacherId());
        }

        PhysicalTestRecord testRecord = null;
        List<Map<String, Object>> itemDetailsList = new ArrayList<>();

        Optional<PhysicalTestRecord> recordOpt = physicalTestRepository.findLatestByStudentId(studentId);
        if (recordOpt.isPresent()) {
            testRecord = recordOpt.get();
            List<PhysicalScoreDetail> details = physicalTestRepository.findByRecordId(testRecord.getId());

            for (PhysicalScoreDetail detail : details) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("detailId", detail.getId());
                itemMap.put("itemId", detail.getItemId());
                itemMap.put("rawValue", detail.getRawValue());
                itemMap.put("convertedScore", detail.getConvertedScore());
                itemMap.put("level", detail.getLevel());
                itemMap.put("remark", detail.getRemark());

                Optional<PhysicalTestItem> itemOpt = physicalTestRepository.findItemById(detail.getItemId());
                if (itemOpt.isPresent()) {
                    PhysicalTestItem item = itemOpt.get();
                    itemMap.put("itemName", item.getItemName());
                    itemMap.put("itemCode", item.getItemCode());
                    itemMap.put("unit", item.getUnit());
                }

                itemDetailsList.add(itemMap);
            }
        }

        return Map.of(
                "student", studentInfo,
                "testRecord", testRecord != null ? testRecord : new HashMap<>(),
                "items", itemDetailsList,
                "generatedAt", LocalDateTime.now()
        );
    }
}
