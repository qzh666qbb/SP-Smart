package com.example.training.schedule.domain.repository;

import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.model.ScheduleSeries;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    
    ScheduleSeries saveScheduleSeries(ScheduleSeries series);
    
    Optional<ScheduleSeries> findScheduleSeriesById(Long id);
    
    List<ScheduleSeries> findByTeacherId(Long teacherId);
    
    List<ScheduleSeries> findByClassGroupId(Long classGroupId);
    
    Lesson saveLesson(Lesson lesson);
    
    Optional<Lesson> findLessonById(Long id);
    
    Optional<Lesson> findByLessonNo(String lessonNo);
    
    List<Lesson> findByTeacherIdAndDate(Long teacherId, LocalDate date);

    List<Lesson> findByTeacherIdAndDateRange(Long teacherId, LocalDate startDate, LocalDate endDate);
    
    List<Lesson> findByClassGroupIdAndDateRange(Long classGroupId, LocalDate startDate, LocalDate endDate);
    
    List<Lesson> findByStudentIdAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate);

    boolean existsLessonBySeriesIdAndDate(Long seriesId, LocalDate date);

    List<Long> findStudentIdsByScheduleSeriesId(Long seriesId);

    void replaceScheduleSeriesStudents(Long seriesId, List<Long> studentIds, Long createdBy);
    
    LessonStudent saveLessonStudent(LessonStudent lessonStudent);
    
    Optional<LessonStudent> findLessonStudent(Long lessonId, Long studentId);
    
    List<LessonStudent> findByLessonId(Long lessonId);
    
    List<LessonStudent> findByStudentId(Long studentId);
    
    void deleteScheduleSeries(Long id);

    void softDeletePendingLessonsBySeries(Long seriesId, LocalDate fromDate);

    int markAbsentForCompletedLessons(LocalDateTime threshold);

    List<LessonStudent> findLessonStudentsByLessonId(Long lessonId);
}
