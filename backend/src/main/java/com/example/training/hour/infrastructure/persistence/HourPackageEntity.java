package com.example.training.hour.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hour_package")
public class HourPackageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String packageCode;
    private String packageName;
    private String packageType;
    private BigDecimal totalHours;
    private BigDecimal price;
    private Integer validMonths;
    private String description;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
