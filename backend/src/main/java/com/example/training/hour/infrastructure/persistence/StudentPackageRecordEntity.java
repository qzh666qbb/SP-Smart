package com.example.training.hour.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("student_package_record")
public class StudentPackageRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long packageId;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private BigDecimal totalHours;
    private BigDecimal remainingHours;
    private BigDecimal consumedHours;
    private BigDecimal refundedHours;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
