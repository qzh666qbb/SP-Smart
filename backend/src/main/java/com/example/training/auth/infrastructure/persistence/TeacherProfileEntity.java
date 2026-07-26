package com.example.training.auth.infrastructure.persistence;

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
@TableName("teacher_profile")
public class TeacherProfileEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String teacherNo;
    private String name;
    private String gender;
    private String avatarUrl;
    private String introduction;
    private String employmentStatus;
    private LocalDate joinedDate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
