package com.example.training.schedule.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("lesson")
public class LessonEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String lessonNo;
    private Long scheduleSeriesId;
    private Long courseId;
    private Long classGroupId;
    private Long teacherId;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String location;
    private String cancelReason;
    private LocalDateTime completedAt;
    private Long completedBy;
    private String sourceType;
    private Integer version;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
