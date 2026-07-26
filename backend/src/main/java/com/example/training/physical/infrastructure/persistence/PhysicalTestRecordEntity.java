package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
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
}
