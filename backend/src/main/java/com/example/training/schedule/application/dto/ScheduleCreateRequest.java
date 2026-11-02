package com.example.training.schedule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
    
    private Long teacherId;

    private Long assistantTeacherId1;

    private Long assistantTeacherId2;

    private List<Long> studentIds;
    
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
    @DecimalMin(value = "0.01", message = "消耗课时必须大于0")
    private BigDecimal consumeHours;

    @AssertTrue(message = "结束时间必须晚于开始时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }

    @AssertTrue(message = "结束日期不能早于开始日期")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "周循环排课必须选择星期并设置结束日期")
    public boolean isWeeklyConfigValid() {
        return !"WEEKLY".equals(recurrenceType)
                || (endDate != null && getWeekdays() != null && !getWeekdays().isBlank());
    }

    @AssertTrue(message = "循环排课时间范围不能超过366天")
    public boolean isRangeLengthValid() {
        return startDate == null || endDate == null
                || ChronoUnit.DAYS.between(startDate, endDate) <= 366;
    }

    public String getSeriesName() { return seriesName; }    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }
    public Long getCourseId() { return courseId; }    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getClassGroupId() { return classGroupId; }    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }
    public Long getTeacherId() { return teacherId; }    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getAssistantTeacherId1() { return assistantTeacherId1; }    public void setAssistantTeacherId1(Long assistantTeacherId1) { this.assistantTeacherId1 = assistantTeacherId1; }
    public Long getAssistantTeacherId2() { return assistantTeacherId2; }    public void setAssistantTeacherId2(Long assistantTeacherId2) { this.assistantTeacherId2 = assistantTeacherId2; }
    public List<Long> getStudentIds() { return studentIds; }    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }
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
