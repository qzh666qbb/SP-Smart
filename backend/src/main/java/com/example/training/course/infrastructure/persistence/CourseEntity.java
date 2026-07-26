package com.example.training.course.infrastructure.persistence;

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
@TableName("course")
public class CourseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String courseCode;
    private String courseName;
    private String courseType;
    private Integer defaultDurationMinutes;
    private java.math.BigDecimal defaultConsumeHours;
    private String description;
    private String status;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
