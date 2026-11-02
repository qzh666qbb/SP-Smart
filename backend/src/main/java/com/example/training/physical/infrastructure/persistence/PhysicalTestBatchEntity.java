package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("physical_test_batch")
public class PhysicalTestBatchEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String batchName;
    private String batchCode;
    private Long templateId;
    private LocalDate testDate;
    private String location;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
