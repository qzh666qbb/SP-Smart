package com.example.training.student.infrastructure.persistence;

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
@TableName("parent_student_relation")
public class ParentStudentRelationEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;
    private Long studentId;
    private String relationType;
    private Boolean isPrimary;
    private String bindStatus;
    private LocalDateTime boundAt;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
