package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("physical_score_detail")
public class PhysicalScoreDetailEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long recordId;
    private Long itemId;
    private BigDecimal rawValue;
    private BigDecimal convertedScore;
    private String level;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
