package com.example.training.physical.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.physical.application.dto.PhysicalTestCreateRequest;
import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.repository.PhysicalTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalTestApplicationService {
    
    private final PhysicalTestRepository physicalTestRepository;
    
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
        
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (PhysicalTestCreateRequest.ScoreDetail detail : request.getScores()) {
            PhysicalScoreDetail scoreDetail = PhysicalScoreDetail.builder()
                    .recordId(savedRecord.getId())
                    .itemId(detail.getItemId())
                    .rawValue(detail.getRawValue())
                    .convertedScore(detail.getConvertedScore())
                    .level(detail.getLevel())
                    .remark(detail.getRemark())
                    .build();
            
            physicalTestRepository.saveScoreDetail(scoreDetail);
            
            if (detail.getConvertedScore() != null) {
                totalScore = totalScore.add(detail.getConvertedScore());
            }
        }
        
        savedRecord.setTotalScore(totalScore.setScale(1, RoundingMode.HALF_UP));
        savedRecord.setOverallLevel(calculateOverallLevel(totalScore));
        savedRecord.setStatus("SUBMITTED");
        
        physicalTestRepository.saveRecord(savedRecord);
        log.info("创建体测记录: recordNo={}, studentId={}, totalScore={}", recordNo, request.getStudentId(), totalScore);
        
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
        
        BigDecimal totalScore = BigDecimal.ZERO;
        for (PhysicalTestCreateRequest.ScoreDetail detail : request.getScores()) {
            PhysicalScoreDetail scoreDetail = PhysicalScoreDetail.builder()
                    .recordId(recordId)
                    .itemId(detail.getItemId())
                    .rawValue(detail.getRawValue())
                    .convertedScore(detail.getConvertedScore())
                    .level(detail.getLevel())
                    .remark(detail.getRemark())
                    .build();
            
            physicalTestRepository.saveScoreDetail(scoreDetail);
            
            if (detail.getConvertedScore() != null) {
                totalScore = totalScore.add(detail.getConvertedScore());
            }
        }
        
        record.setTotalScore(totalScore.setScale(1, RoundingMode.HALF_UP));
        record.setOverallLevel(calculateOverallLevel(totalScore));
        
        return physicalTestRepository.saveRecord(record);
    }
    
    public PhysicalTestRecord getLatestRecord(Long studentId) {
        return physicalTestRepository.findLatestByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PHYSICAL_TEST_NOT_FOUND));
    }
}
