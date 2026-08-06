package com.example.training.report.application.service;

import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final StudentRepository studentRepository;
    private final PhysicalTestRepository physicalTestRepository;

    public Map<String, Object> generateAttendanceReport(Long classGroupId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("type", "ATTENDANCE");
        report.put("classGroupId", classGroupId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("generatedAt", java.time.LocalDateTime.now());

        log.info("生成出勤报表: classGroupId={}, startDate={}, endDate={}", classGroupId, startDate, endDate);
        return report;
    }

    public Map<String, Object> generateHourReport(Long studentId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("type", "HOUR");
        report.put("studentId", studentId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("generatedAt", java.time.LocalDateTime.now());

        log.info("生成课时报表: studentId={}, startDate={}, endDate={}", studentId, startDate, endDate);
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
