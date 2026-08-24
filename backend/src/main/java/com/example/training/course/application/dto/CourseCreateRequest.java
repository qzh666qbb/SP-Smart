package com.example.training.course.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateRequest {
    
    @NotBlank(message = "课程编码不能为空")
    private String courseCode;
    
    @NotBlank(message = "课程名称不能为空")
    private String courseName;
    
    @NotBlank(message = "课程类型不能为空")
    @Pattern(regexp = "^(ONE_TO_ONE|SMALL_CLASS|GROUP)$", message = "课程类型必须是ONE_TO_ONE、SMALL_CLASS或GROUP")
    private String courseType;
    
    @NotNull(message = "默认时长不能为空")
    private Integer defaultDurationMinutes;
    
    @NotNull(message = "默认消耗课时不能为空")
    private BigDecimal defaultConsumeHours;
    
    private String description;

    public String getCourseCode() { return courseCode; }    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseType() { return courseType; }    public void setCourseType(String courseType) { this.courseType = courseType; }
    public Integer getDefaultDurationMinutes() { return defaultDurationMinutes; }    public void setDefaultDurationMinutes(Integer defaultDurationMinutes) { this.defaultDurationMinutes = defaultDurationMinutes; }
    public BigDecimal getDefaultConsumeHours() { return defaultConsumeHours; }    public void setDefaultConsumeHours(BigDecimal defaultConsumeHours) { this.defaultConsumeHours = defaultConsumeHours; }
    public String getDescription() { return description; }    public void setDescription(String description) { this.description = description; }
}
