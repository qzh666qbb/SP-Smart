package com.example.training.student.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getRelationType() { return relationType; }    public void setRelationType(String relationType) { this.relationType = relationType; }
    public Boolean getIsPrimary() { return isPrimary; }    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    public String getBindStatus() { return bindStatus; }    public void setBindStatus(String bindStatus) { this.bindStatus = bindStatus; }
    public LocalDateTime getBoundAt() { return boundAt; }    public void setBoundAt(LocalDateTime boundAt) { this.boundAt = boundAt; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
