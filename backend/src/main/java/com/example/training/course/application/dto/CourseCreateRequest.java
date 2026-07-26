package com.example.training.course.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
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
}
