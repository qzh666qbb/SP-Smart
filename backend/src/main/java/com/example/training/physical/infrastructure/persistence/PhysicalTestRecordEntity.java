package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("physical_test_record")
public class PhysicalTestRecordEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String recordNo;
    private Long batchId;
    private Long studentId;
    private LocalDate testDate;
    private Long testerId;
    private BigDecimal totalScore;
    private String overallLevel;
    private String status;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getRecordNo() { return recordNo; }    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public Long getBatchId() { return batchId; }    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public LocalDate getTestDate() { return testDate; }    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }
    public Long getTesterId() { return testerId; }    public void setTesterId(Long testerId) { this.testerId = testerId; }
    public BigDecimal getTotalScore() { return totalScore; }    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getOverallLevel() { return overallLevel; }    public void setOverallLevel(String overallLevel) { this.overallLevel = overallLevel; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
