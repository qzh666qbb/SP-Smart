package com.example.training.course.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    private Long id;
    private String courseCode;
    private String courseName;
    private String courseType;
    private Integer defaultDurationMinutes;
    private java.math.BigDecimal defaultConsumeHours;
    private String description;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isEnabled() {
        return "ENABLED".equals(status);
    }

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getCourseCode() { return courseCode; }    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseType() { return courseType; }    public void setCourseType(String courseType) { this.courseType = courseType; }
    public Integer getDefaultDurationMinutes() { return defaultDurationMinutes; }    public void setDefaultDurationMinutes(Integer defaultDurationMinutes) { this.defaultDurationMinutes = defaultDurationMinutes; }
    public java.math.BigDecimal getDefaultConsumeHours() { return defaultConsumeHours; }
    public void setDefaultConsumeHours(java.math.BigDecimal defaultConsumeHours) { this.defaultConsumeHours = defaultConsumeHours; }
    public String getDescription() { return description; }    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
