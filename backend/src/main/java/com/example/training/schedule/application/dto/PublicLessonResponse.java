package com.example.training.schedule.application.dto;

import com.example.training.schedule.domain.model.Lesson;
import java.time.LocalDate;
import java.time.LocalTime;

/** 家长可见课表，只提供排课信息，不包含学员和出勤记录。 */
public record PublicLessonResponse(Long id, Long courseId, String courseName,
        Long teacherId, String teacherName, LocalDate lessonDate,
        LocalTime startTime, LocalTime endTime, String status, String location) {
    public static PublicLessonResponse from(Lesson lesson) {
        return new PublicLessonResponse(lesson.getId(), lesson.getCourseId(), lesson.getCourseName(),
                lesson.getTeacherId(), lesson.getTeacherName(), lesson.getLessonDate(),
                lesson.getStartTime(), lesson.getEndTime(), lesson.getStatus(), lesson.getLocation());
    }
}
