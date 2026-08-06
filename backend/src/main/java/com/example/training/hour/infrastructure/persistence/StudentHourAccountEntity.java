package com.example.training.hour.infrastructure.persistence;

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
@TableName("student_hour_account")
public class StudentHourAccountEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    private BigDecimal totalRecharged;
    private BigDecimal totalConsumed;
    private BigDecimal totalRefunded;
    private BigDecimal remainingHours;
    
    @TableField("paid_hours")
    private BigDecimal paidHours;
    
    @TableField("gift_hours")
    private BigDecimal giftHours;
    
    @TableField("experience_hours")
    private BigDecimal experienceHours;
    
    @TableField("activity_hours")
    private BigDecimal activityHours;
    
    private Integer version;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
