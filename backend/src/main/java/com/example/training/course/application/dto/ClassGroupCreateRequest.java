package com.example.training.course.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassGroupCreateRequest {
    
    @NotBlank(message = "班级编号不能为空")
    private String classCode;
    
    @NotBlank(message = "班级名称不能为空")
    private String className;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    private Long primaryTeacherId;
    
    private Integer maxStudents;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String remark;
    
    private String status;

    public String getClassCode() { return classCode; }    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getClassName() { return className; }    public void setClassName(String className) { this.className = className; }
    public Long getCourseId() { return courseId; }    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getPrimaryTeacherId() { return primaryTeacherId; }    public void setPrimaryTeacherId(Long primaryTeacherId) { this.primaryTeacherId = primaryTeacherId; }
    public Integer getMaxStudents() { return maxStudents; }    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public LocalDate getStartDate() { return startDate; }    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
}
