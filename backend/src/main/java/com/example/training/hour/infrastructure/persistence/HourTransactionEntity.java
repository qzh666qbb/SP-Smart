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
@TableName("hour_transaction")
public class HourTransactionEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String transactionNo;
    private Long studentId;
    private String transactionType;
    private BigDecimal changeAmount;
    private BigDecimal beforeBalance;
    private BigDecimal afterBalance;
    private Long lessonId;
    private Long lessonStudentId;
    private Long relatedTransactionId;
    private String reason;
    private Long operatorId;
    private String operatorRole;
    private String idempotencyKey;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
