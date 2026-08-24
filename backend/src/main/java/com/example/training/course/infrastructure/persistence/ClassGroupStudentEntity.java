package com.example.training.course.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("class_group_student")
public class ClassGroupStudentEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long classGroupId;
    private Long studentId;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private String status;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getClassGroupId() { return classGroupId; }    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }
    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public LocalDateTime getJoinedAt() { return joinedAt; }    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    public LocalDateTime getLeftAt() { return leftAt; }    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
