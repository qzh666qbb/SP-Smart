package com.example.training.physical.domain.repository;

import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PhysicalTestRepository {
    
    PhysicalTestItem saveItem(PhysicalTestItem item);
    
    Optional<PhysicalTestItem> findItemById(Long id);
    
    List<PhysicalTestItem> findAllEnabledItems();
    
    PhysicalTestRecord saveRecord(PhysicalTestRecord record);
    
    Optional<PhysicalTestRecord> findRecordById(Long id);
    
    Optional<PhysicalTestRecord> findRecordByNo(String recordNo);
    
    List<PhysicalTestRecord> findByStudentId(Long studentId);
    
    List<PhysicalTestRecord> findByBatchId(Long batchId);
    
    PhysicalScoreDetail saveScoreDetail(PhysicalScoreDetail detail);
    
    List<PhysicalScoreDetail> findByRecordId(Long recordId);
    
    void deleteScoreDetails(Long recordId);
    
    List<PhysicalTestRecord> findByTesterId(Long testerId);
    
    Optional<PhysicalTestRecord> findLatestByStudentId(Long studentId);
}
