package com.example.training.physical.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.physical.application.dto.PhysicalTestCreateRequest;
import com.example.training.physical.application.dto.PhysicalTestBatchCreateRequest;
import com.example.training.physical.domain.model.PhysicalTestBatch;
import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import com.example.training.physical.domain.service.PhysicalScoringEngine;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalTestApplicationService {
    private static final Logger log = LoggerFactory.getLogger(PhysicalTestApplicationService.class);

    
    private final PhysicalTestRepository physicalTestRepository;
    private final PhysicalScoringEngine scoringEngine;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public PhysicalTestRecord createTestRecord(PhysicalTestCreateRequest request, CurrentUser currentUser) {
        requireAvailableBatch(request.getBatchId());
        Student student = requireStudentAndItems(request);
        String recordNo = "PHY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        PhysicalTestRecord record = PhysicalTestRecord.builder()
                .recordNo(recordNo)
                .batchId(request.getBatchId())
                .studentId(request.getStudentId())
                .testDate(request.getTestDate())
                .testerId(currentUser.getUserId())
                .status("DRAFT")
                .remark(request.getRemark())
                .build();
        
        PhysicalTestRecord savedRecord = physicalTestRepository.saveRecord(record);
        
        String gender = student != null ? student.getGender() : null;
        LocalDate birthDate = student != null ? student.getBirthDate() : null;
        LocalDate testDate = request.getTestDate() != null ? request.getTestDate() : LocalDate.now();
        
        BigDecimal totalScore = BigDecimal.ZERO;
        List<PhysicalScoringEngine.ScoringResult> scoringResults = new ArrayList<>();
        
        for (PhysicalTestCreateRequest.ScoreDetail detail : request.getScores()) {
            BigDecimal engineScore = null;
            String engineLevel = null;
            
            if (student != null && gender != null && birthDate != null) {
                PhysicalScoringEngine.ScoringResult result = scoringEngine.scoreItem(
                        detail.getItemId(), detail.getRawValue(), gender, birthDate, testDate);
                if (result.isValid()) {
                    engineScore = result.getScoreValue();
                    engineLevel = result.getLevel();
                    scoringResults.add(result);
                }
            }
            
            BigDecimal convertedScore = (engineScore != null) ? engineScore : detail.getConvertedScore();
            String level = (engineLevel != null) ? engineLevel : detail.getLevel();
            
            PhysicalScoreDetail scoreDetail = PhysicalScoreDetail.builder()
                    .recordId(savedRecord.getId())
                    .itemId(detail.getItemId())
                    .rawValue(detail.getRawValue())
                    .convertedScore(convertedScore)
                    .level(level)
                    .remark(detail.getRemark())
                    .build();
            
            physicalTestRepository.saveScoreDetail(scoreDetail);
            
            if (convertedScore != null) {
                totalScore = totalScore.add(convertedScore);
            }
        }
        
        BigDecimal engineTotal = scoringEngine.calculateOverallScore(scoringResults);
        int itemCount = scoringResults.size();
        BigDecimal finalTotal;
        String finalLevel;
        
        if (itemCount > 0 && engineTotal.compareTo(BigDecimal.ZERO) > 0) {
            finalTotal = engineTotal;
            finalLevel = scoringEngine.calculateOverallLevel(engineTotal, itemCount);
        } else {
            finalTotal = totalScore.setScale(1, RoundingMode.HALF_UP);
            finalLevel = calculateOverallLevel(totalScore);
        }
        
        savedRecord.setTotalScore(finalTotal);
        savedRecord.setOverallLevel(finalLevel);
        savedRecord.setStatus("SUBMITTED");
        
        physicalTestRepository.saveRecord(savedRecord);
        log.info("创建体测记录: recordNo={}, studentId={}, totalScore={}", recordNo, request.getStudentId(), finalTotal);
        
        return enrichRecord(savedRecord);
    }
    
    private String calculateOverallLevel(BigDecimal totalScore) {
        if (totalScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return "EXCELLENT";
        } else if (totalScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "GOOD";
        } else if (totalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "PASS";
        } else {
            return "FAIL";
        }
    }
    
    @Transactional
    public PhysicalTestRecord confirmRecord(Long recordId, CurrentUser currentUser) {
        PhysicalTestRecord record = physicalTestRepository.findRecordById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
        
        record.setStatus("CONFIRMED");
        return enrichRecord(physicalTestRepository.saveRecord(record));
    }
    
    public PhysicalTestRecord getRecordById(Long recordId) {
        PhysicalTestRecord record = physicalTestRepository.findRecordById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
        return enrichRecord(record);
    }
    
    public List<PhysicalTestRecord> getRecordsByStudent(Long studentId) {
        return physicalTestRepository.findByStudentId(studentId).stream().map(this::enrichRecord).toList();
    }
    
    public List<PhysicalTestItem> getAllEnabledItems() {
        return physicalTestRepository.findAllEnabledItems();
    }

    public List<PhysicalTestBatch> getAvailableBatches() {
        return physicalTestRepository.findAvailableBatches();
    }

    @Transactional
    public PhysicalTestBatch createBatch(PhysicalTestBatchCreateRequest request, CurrentUser currentUser) {
        Long templateId = physicalTestRepository.findDefaultTemplateId();
        if (templateId == null) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "没有可用的体测模板");
        }
        String code = "PHY-" + request.getTestDate().toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return physicalTestRepository.saveBatch(PhysicalTestBatch.builder()
                .batchName(request.getBatchName().trim()).batchCode(code).templateId(templateId)
                .testDate(request.getTestDate()).location(request.getLocation())
                .status("IN_PROGRESS").createdBy(currentUser.getUserId()).build());
    }
    
    public List<PhysicalScoreDetail> getScoreDetails(Long recordId) {
        return physicalTestRepository.findByRecordId(recordId);
    }
    
    public List<PhysicalTestRecord> listTestRecords(Long testerId) {
        return listTestRecordsPage(testerId, null, 1, Integer.MAX_VALUE).getList();
    }

    public PageResult<PhysicalTestRecord> listTestRecordsPage(Long testerId, String keyword, int pageNum, int pageSize) {
        return listTestRecordsPage(testerId, null, keyword, pageNum, pageSize);
    }

    public PageResult<PhysicalTestRecord> listTestRecordsPageForTeacher(
            Long teacherProfileId, String keyword, int pageNum, int pageSize) {
        return listTestRecordsPage(null, teacherProfileId, keyword, pageNum, pageSize);
    }

    private PageResult<PhysicalTestRecord> listTestRecordsPage(
            Long testerId, Long teacherProfileId, String keyword, int pageNum, int pageSize) {
        int pn = Math.max(1, pageNum);
        int ps = Math.min(500, Math.max(1, pageSize));
        List<PhysicalTestRecord> all = physicalTestRepository.findByTesterId(testerId).stream()
                .filter(record -> teacherProfileId == null
                        || studentRepository.canTeacherAccessStudent(teacherProfileId, record.getStudentId()))
                .toList();
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        java.util.Set<Long> matchedStudentIds = null;
        if (kw != null && !kw.isEmpty()) {
            matchedStudentIds = studentRepository.findAll().stream()
                    .filter(s -> contains(s.getName(), kw)
                            || contains(s.getStudentNo(), kw)
                            || contains(s.getPhone(), kw)
                            || contains(s.getParentName(), kw)
                            || contains(s.getParentPhone(), kw))
                    .map(Student::getId)
                    .collect(java.util.stream.Collectors.toSet());
        }
        final java.util.Set<Long> matchedIds = matchedStudentIds;
        List<PhysicalTestRecord> filtered = all.stream()
                .filter(r -> kw == null || kw.isEmpty()
                        || contains(r.getRecordNo(), kw)
                        || contains(r.getOverallLevel(), kw)
                        || contains(r.getStatus(), kw)
                        || contains(r.getRemark(), kw)
                        || contains(r.getTestDate() == null ? null : r.getTestDate().toString(), kw)
                        || (r.getStudentId() != null && matchedIds != null && matchedIds.contains(r.getStudentId())))
                .collect(java.util.stream.Collectors.toList());
        int from = Math.max(0, (pn - 1) * ps);
        int to = Math.min(from + ps, filtered.size());
        List<PhysicalTestRecord> page = from >= filtered.size() ? new ArrayList<>() : new ArrayList<>(filtered.subList(from, to));
        return PageResult.of(page.stream().map(this::enrichRecord).toList(), filtered.size(), pn, ps);
    }

    private static boolean contains(String src, String lowerKw) {
        if (src == null || lowerKw == null) return false;
        return src.toLowerCase().contains(lowerKw);
    }
    
    @Transactional
    public PhysicalTestRecord updateRecord(Long recordId, PhysicalTestCreateRequest request, CurrentUser currentUser) {
        PhysicalTestRecord record = physicalTestRepository.findRecordById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
        if (!record.getStudentId().equals(request.getStudentId())) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "不能修改体测记录所属学员");
        }
        requireAvailableBatch(request.getBatchId());
        Student student = requireStudentAndItems(request);
        
        if ("CONFIRMED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已确认的记录不能修改");
        }
        
        record.setTestDate(request.getTestDate());
        record.setBatchId(request.getBatchId());
        record.setRemark(request.getRemark());
        
        physicalTestRepository.deleteScoreDetails(recordId);
        
        String gender = student != null ? student.getGender() : null;
        LocalDate birthDate = student != null ? student.getBirthDate() : null;
        LocalDate testDate = request.getTestDate() != null ? request.getTestDate() : LocalDate.now();
        
        BigDecimal totalScore = BigDecimal.ZERO;
        List<PhysicalScoringEngine.ScoringResult> scoringResults = new ArrayList<>();
        
        for (PhysicalTestCreateRequest.ScoreDetail detail : request.getScores()) {
            BigDecimal engineScore = null;
            String engineLevel = null;
            
            if (student != null && gender != null && birthDate != null) {
                PhysicalScoringEngine.ScoringResult result = scoringEngine.scoreItem(
                        detail.getItemId(), detail.getRawValue(), gender, birthDate, testDate);
                if (result.isValid()) {
                    engineScore = result.getScoreValue();
                    engineLevel = result.getLevel();
                    scoringResults.add(result);
                }
            }
            
            BigDecimal convertedScore = (engineScore != null) ? engineScore : detail.getConvertedScore();
            String level = (engineLevel != null) ? engineLevel : detail.getLevel();
            
            PhysicalScoreDetail scoreDetail = PhysicalScoreDetail.builder()
                    .recordId(recordId)
                    .itemId(detail.getItemId())
                    .rawValue(detail.getRawValue())
                    .convertedScore(convertedScore)
                    .level(level)
                    .remark(detail.getRemark())
                    .build();
            
            physicalTestRepository.saveScoreDetail(scoreDetail);
            
            if (convertedScore != null) {
                totalScore = totalScore.add(convertedScore);
            }
        }
        
        BigDecimal engineTotal = scoringEngine.calculateOverallScore(scoringResults);
        int itemCount = scoringResults.size();
        BigDecimal finalTotal;
        String finalLevel;
        
        if (itemCount > 0 && engineTotal.compareTo(BigDecimal.ZERO) > 0) {
            finalTotal = engineTotal;
            finalLevel = scoringEngine.calculateOverallLevel(engineTotal, itemCount);
        } else {
            finalTotal = totalScore.setScale(1, RoundingMode.HALF_UP);
            finalLevel = calculateOverallLevel(totalScore);
        }
        
        record.setTotalScore(finalTotal);
        record.setOverallLevel(finalLevel);
        
        return enrichRecord(physicalTestRepository.saveRecord(record));
    }
    
    public PhysicalTestRecord getLatestRecord(Long studentId) {
        return enrichRecord(physicalTestRepository.findLatestByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND)));
    }

    private PhysicalTestRecord enrichRecord(PhysicalTestRecord record) {
        if (record == null) return null;
        if (record.getStudentId() != null) {
            studentRepository.findById(record.getStudentId()).ifPresent(student -> record.setStudentName(student.getName()));
        }
        if (record.getTesterId() != null) {
            userRepository.findTeacherProfileByUserId(record.getTesterId())
                    .ifPresent(profile -> record.setTeacherName(profile.getName()));
        }
        if (record.getId() != null) {
            List<PhysicalScoreDetail> details = physicalTestRepository.findByRecordId(record.getId());
            for (PhysicalScoreDetail detail : details) {
                physicalTestRepository.findItemById(detail.getItemId()).ifPresent(item -> {
                    detail.setItemName(item.getItemName());
                    detail.setUnit(item.getUnit());
                });
            }
            record.setDetails(details);
        }
        return record;
    }

    private Student requireStudentAndItems(PhysicalTestCreateRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        for (PhysicalTestCreateRequest.ScoreDetail detail : request.getScores()) {
            PhysicalTestItem item = physicalTestRepository.findItemById(detail.getItemId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_VALID_ERROR, "体测项目不存在"));
            if (!item.isEnabled()) {
                throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "体测项目已停用");
            }
        }
        return student;
    }

    public Long resolveLegacyBatchId() {
        List<PhysicalTestBatch> available = physicalTestRepository.findAvailableBatches();
        if (available.size() == 1) return available.get(0).getId();
        throw new BusinessException(ErrorCode.PARAM_VALID_ERROR,
                available.isEmpty() ? "暂无可用体测批次，请联系管理员"
                        : "旧版无法选择体测批次，请更新小程序后选择本次批次");
    }

    private void requireAvailableBatch(Long batchId) {
        PhysicalTestBatch batch = physicalTestRepository.findBatchById(batchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_VALID_ERROR, "体测批次不存在"));
        if (!"PENDING".equals(batch.getStatus()) && !"IN_PROGRESS".equals(batch.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "体测批次不可用");
        }
    }

    @Transactional
    public void deleteTestRecord(Long recordId) {
        PhysicalTestRecord record = physicalTestRepository.findRecordById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
        record.setDeletedAt(java.time.LocalDateTime.now());
        physicalTestRepository.saveRecord(record);
        log.info("删除体测记录: recordId={}", recordId);
    }
}
