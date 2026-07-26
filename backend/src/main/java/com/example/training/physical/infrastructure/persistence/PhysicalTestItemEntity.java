package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("physical_test_item")
public class PhysicalTestItemEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String itemCode;
    private String itemName;
    private String itemType;
    private String unit;
    private Boolean genderSpecific;
    private Integer ageMin;
    private Integer ageMax;
    private String description;
    private String status;
    private Integer sortOrder;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
