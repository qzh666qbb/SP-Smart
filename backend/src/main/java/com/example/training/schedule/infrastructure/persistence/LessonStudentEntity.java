package com.example.training.schedule.infrastructure.persistence;

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
@TableName("lesson_student")
public class LessonStudentEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long lessonId;
    private Long studentId;
    private String attendanceStatus;
    private LocalDateTime signInAt;
    private BigDecimal consumeHours;
    private Boolean shouldConsume;
    private Long hourTransactionId;
    private String attendanceRemark;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    private Integer rating;
    private String ratingComment;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
