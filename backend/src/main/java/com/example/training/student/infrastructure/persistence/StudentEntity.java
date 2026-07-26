package com.example.training.student.infrastructure.persistence;

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
@TableName("student")
public class StudentEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String studentNo;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String phone;
    private Long primaryTeacherId;
    private LocalDate enrollmentDate;
    private String status;
    private String medicalNotes;
    private String remark;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
}
