package com.example.training.physical.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.physical.application.dto.PhysicalTestCreateRequest;
import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import com.example.training.physical.domain.service.PhysicalScoringEngine;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    
    private final PhysicalTestRepository physicalTestRepository;
    private final PhysicalScoringEngine scoringEngine;
    private final StudentRepository studentRepository;
    
    @Transactional
    public PhysicalTestRecord createTestRecord(PhysicalTestCreateRequest request, CurrentUser currentUser) {
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
        
        Student student = studentRepository.findById(request.getStudentId()).orElse(null);
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
        
        return savedRecord;
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
        return physicalTestRepository.saveRecord(record);
    }
    
    public PhysicalTestRecord getRecordById(Long recordId) {
        return physicalTestRepository.findRecordById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
    }
    
    public List<PhysicalTestRecord> getRecordsByStudent(Long studentId) {
        return physicalTestRepository.findByStudentId(studentId);
    }
    
    public List<PhysicalTestItem> getAllEnabledItems() {
        return physicalTestRepository.findAllEnabledItems();
    }
    
    public List<PhysicalScoreDetail> getScoreDetails(Long recordId) {
        return physicalTestRepository.findByRecordId(recordId);
    }
    
    public List<PhysicalTestRecord> listTestRecords(Long testerId) {
        return physicalTestRepository.findByTesterId(testerId);
    }
    
    @Transactional
    public PhysicalTestRecord updateRecord(Long recordId, PhysicalTestCreateRequest request, CurrentUser currentUser) {
        PhysicalTestRecord record = physicalTestRepository.findRecordById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
        
        if ("CONFIRMED".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已确认的记录不能修改");
        }
        
        record.setTestDate(request.getTestDate());
        record.setRemark(request.getRemark());
        
        physicalTestRepository.deleteScoreDetails(recordId);
        
        Student student = studentRepository.findById(request.getStudentId()).orElse(null);
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
        
        return physicalTestRepository.saveRecord(record);
    }
    
    public PhysicalTestRecord getLatestRecord(Long studentId) {
        return physicalTestRepository.findLatestByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
    }
}
