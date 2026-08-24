package com.example.training.schedule.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {
    
    @NotNull(message = "课程实例ID不能为空")
    private Long lessonId;
    
    @NotNull(message = "学员ID不能为空")
    private Long studentId;
    
    @NotBlank(message = "出勤状态不能为空")
    @Pattern(regexp = "^(PRESENT|LEAVE|ABSENT|CANCELLED)$", message = "出勤状态必须是PRESENT、LEAVE、ABSENT或CANCELLED")
    private String attendanceStatus;
    
    private String remark;

    public Long getLessonId() { return lessonId; }    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getAttendanceStatus() { return attendanceStatus; }    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public String getRemark() { return remark; }    public void setRemark(String remark) { this.remark = remark; }
}
