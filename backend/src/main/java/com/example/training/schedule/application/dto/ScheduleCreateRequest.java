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
import java.util.Collection;
import java.util.List;

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
    
    private Object weekdays;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
    
    @NotNull(message = "消耗课时不能为空")
    private BigDecimal consumeHours;

    public String getSeriesName() { return seriesName; }    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }
    public Long getCourseId() { return courseId; }    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getClassGroupId() { return classGroupId; }    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }
    public Long getTeacherId() { return teacherId; }    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getRecurrenceType() { return recurrenceType; }    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

    public String getWeekdays() {
        if (weekdays == null) return null;
        if (weekdays instanceof String s) return s;
        if (weekdays instanceof Collection<?> c) {
            List<String> parts = c.stream().filter(x -> x != null).map(Object::toString)
                    .filter(s -> !s.isBlank()).toList();
            return parts.isEmpty() ? null : String.join(",", parts);
        }
        return weekdays.toString();
    }

    @SuppressWarnings("unchecked")
    public void setWeekdays(Object weekdays) { this.weekdays = weekdays; }

    public LocalDate getStartDate() { return startDate; }    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalTime getStartTime() { return startTime; }    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public BigDecimal getConsumeHours() { return consumeHours; }    public void setConsumeHours(BigDecimal consumeHours) { this.consumeHours = consumeHours; }
}
