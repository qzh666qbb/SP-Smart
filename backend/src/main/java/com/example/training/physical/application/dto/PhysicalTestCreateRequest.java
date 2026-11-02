package com.example.training.physical.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTestCreateRequest {
    
    @NotNull(message = "批次ID不能为空")
    private Long batchId;
    
    @NotNull(message = "学员ID不能为空")
    private Long studentId;
    
    @NotNull(message = "测试日期不能为空")
    private LocalDate testDate;
    
    @NotNull(message = "成绩详情不能为空")
    @NotEmpty(message = "成绩详情不能为空")
    @Valid
    private List<ScoreDetail> scores;
    
    private String remark;
    
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDetail {
        @NotNull(message = "指标ID不能为空")
        private Long itemId;
        
        @NotNull(message = "测试值不能为空")
        @DecimalMin(value = "0", message = "测试值不能小于0")
        private BigDecimal rawValue;
        
        private BigDecimal convertedScore;
        
        private String level;
        
        private String remark;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public BigDecimal getRawValue() { return rawValue; }
        public void setRawValue(BigDecimal rawValue) { this.rawValue = rawValue; }
        public BigDecimal getConvertedScore() { return convertedScore; }
        public void setConvertedScore(BigDecimal convertedScore) { this.convertedScore = convertedScore; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public LocalDate getTestDate() { return testDate; }
    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }
    public List<ScoreDetail> getScores() { return scores; }
    public void setScores(List<ScoreDetail> scores) { this.scores = scores; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
