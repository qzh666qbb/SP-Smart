package com.example.training.course.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("class_group")
public class ClassGroupEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String classCode;
    private String className;
    private Long courseId;
    private Long primaryTeacherId;
    private Integer maxStudents;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String remark;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
