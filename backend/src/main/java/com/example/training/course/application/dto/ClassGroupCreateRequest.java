package com.example.training.course.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
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
}
