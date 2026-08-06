package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("physical_score_standard")
public class PhysicalScoreStandardEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;
    private String gender;
    private String ageGroup;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer score;
    private String level;
    private BigDecimal scoreValue;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
