package com.example.training.physical.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
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
    private List<ScoreDetail> scores;
    
    private String remark;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDetail {
        @NotNull(message = "指标ID不能为空")
        private Long itemId;
        
        private BigDecimal rawValue;
        
        private BigDecimal convertedScore;
        
        private String level;
        
        private String remark;
    }
}
