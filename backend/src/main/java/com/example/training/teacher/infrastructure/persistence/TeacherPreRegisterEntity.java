package com.example.training.teacher.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("teacher_pre_register")
public class TeacherPreRegisterEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;
    private String realName;
    private Long createdBy;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
