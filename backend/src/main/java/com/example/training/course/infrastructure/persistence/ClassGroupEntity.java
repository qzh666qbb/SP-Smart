package com.example.training.course.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getClassCode() { return classCode; }    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getClassName() { return className; }    public void setClassName(String className) { this.className = className; }
    public Long getCourseId() { return courseId; }    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getPrimaryTeacherId() { return primaryTeacherId; }    public void setPrimaryTeacherId(Long primaryTeacherId) { this.primaryTeacherId = primaryTeacherId; }
    public Integer getMaxStudents() { return maxStudents; }    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
