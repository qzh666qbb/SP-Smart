package com.example.training.course.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourseResponse {
    
    private Long id;
    private String courseCode;
    private String courseName;
    private String courseType;
    private Integer defaultDurationMinutes;
    private BigDecimal defaultConsumeHours;
    private String description;
    private String status;
    private LocalDateTime createdAt;

    public Integer getDuration() { return defaultDurationMinutes; }
    public LocalDateTime getCreateTime() { return createdAt; }
    @JsonIgnore
    public Integer getDefaultDurationMinutesView() { return defaultDurationMinutes; }

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getCourseCode() { return courseCode; }    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseType() { return courseType; }    public void setCourseType(String courseType) { this.courseType = courseType; }
    public Integer getDefaultDurationMinutes() { return defaultDurationMinutes; }    public void setDefaultDurationMinutes(Integer defaultDurationMinutes) { this.defaultDurationMinutes = defaultDurationMinutes; }
    public BigDecimal getDefaultConsumeHours() { return defaultConsumeHours; }    public void setDefaultConsumeHours(BigDecimal defaultConsumeHours) { this.defaultConsumeHours = defaultConsumeHours; }
    public String getDescription() { return description; }    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
