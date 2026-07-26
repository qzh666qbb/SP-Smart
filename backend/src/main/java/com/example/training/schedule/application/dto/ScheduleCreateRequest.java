package com.example.training.schedule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleCreateRequest {
    
    private String seriesName;
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    private Long classGroupId;
    
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;
    
    @NotBlank(message = "重复类型不能为空")
    @Pattern(regexp = "^(NONE|WEEKLY)$", message = "重复类型必须是NONE或WEEKLY")
    private String recurrenceType;
    
    private String weekdays;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
    
    @NotNull(message = "消耗课时不能为空")
    private BigDecimal consumeHours;
}
