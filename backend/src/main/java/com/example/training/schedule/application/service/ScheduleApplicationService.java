package com.example.training.schedule.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.schedule.application.dto.AttendanceRequest;
import com.example.training.schedule.application.dto.ScheduleCreateRequest;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.model.ScheduleSeries;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleApplicationService {
    
    private final ScheduleRepository scheduleRepository;
    
    @Transactional
    public ScheduleSeries createScheduleSeries(ScheduleCreateRequest request, CurrentUser currentUser) {
        ScheduleSeries series = ScheduleSeries.builder()
                .seriesName(request.getSeriesName())
                .courseId(request.getCourseId())
                .classGroupId(request.getClassGroupId())
                .teacherId(request.getTeacherId())
                .recurrenceType(request.getRecurrenceType())
                .weekdays(request.getWeekdays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .consumeHours(request.getConsumeHours())
                .status("ACTIVE")
                .createdBy(currentUser.getUserId())
                .build();
        
        ScheduleSeries savedSeries = scheduleRepository.saveScheduleSeries(series);
        
        if ("WEEKLY".equals(request.getRecurrenceType()) && request.getWeekdays() != null) {
            generateLessonsFromSeries(savedSeries);
        }
        
        return savedSeries;
    }
    
    private void generateLessonsFromSeries(ScheduleSeries series) {
        LocalDate currentDate = series.getStartDate();
        LocalDate endDate = series.getEndDate() != null ? series.getEndDate() : LocalDate.now().plusYears(1);
        
        String[] weekdays = series.getWeekdays().split(",");
        
        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            
            for (String weekday : weekdays) {
                if (dayOfWeek == Integer.parseInt(weekday)) {
                    String lessonNo = "LSN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    
                    Lesson lesson = Lesson.builder()
                            .lessonNo(lessonNo)
                            .scheduleSeriesId(series.getId())
                            .courseId(series.getCourseId())
                            .classGroupId(series.getClassGroupId())
                            .teacherId(series.getTeacherId())
                            .lessonDate(currentDate)
                            .startTime(series.getStartTime())
                            .endTime(series.getEndTime())
                            .status("PENDING")
                            .sourceType("SERIES")
                            .consumeHours(series.getConsumeHours())
                            .createdBy(series.getCreatedBy())
                            .build();
                    
                    scheduleRepository.saveLesson(lesson);
                    log.info("生成课程实例: lessonNo={}, date={}", lessonNo, currentDate);
                }
            }
            
            currentDate = currentDate.plus(1, ChronoUnit.DAYS);
        }
    }
    
    @Transactional
    public void startLesson(Long lessonId, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        
        if (!lesson.isPending()) {
            throw new BusinessException(ErrorCode.LESSON_NOT_PENDING);
        }
        
        lesson.setStatus("IN_PROGRESS");
        lesson.setUpdatedBy(currentUser.getUserId());
        scheduleRepository.saveLesson(lesson);
        log.info("开始课程: lessonId={}", lessonId);
    }
    
    @Transactional
    public void completeLesson(Long lessonId, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        
        if (!lesson.isInProgress()) {
            throw new BusinessException(ErrorCode.LESSON_NOT_PENDING);
        }
        
        lesson.setStatus("COMPLETED");
        lesson.setCompletedAt(LocalDateTime.now());
        lesson.setCompletedBy(currentUser.getUserId());
        lesson.setUpdatedBy(currentUser.getUserId());
        scheduleRepository.saveLesson(lesson);
        log.info("完成课程: lessonId={}", lessonId);
    }
    
    @Transactional
    public void cancelLesson(Long lessonId, String reason, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        
        if (lesson.isCompleted()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已完成的课程不能取消");
        }
        
        lesson.setStatus("CANCELLED");
        lesson.setCancelReason(reason);
        lesson.setUpdatedBy(currentUser.getUserId());
        scheduleRepository.saveLesson(lesson);
        log.info("取消课程: lessonId={}, reason={}", lessonId, reason);
    }
    
    @Transactional
    public void recordAttendance(AttendanceRequest request, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(request.getLessonId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        
        LessonStudent existing = scheduleRepository.findLessonStudent(request.getLessonId(), request.getStudentId())
                .orElse(null);
        
        if (existing == null) {
            LessonStudent lessonStudent = LessonStudent.builder()
                    .lessonId(request.getLessonId())
                    .studentId(request.getStudentId())
                    .attendanceStatus(request.getAttendanceStatus())
                    .shouldConsume("PRESENT".equals(request.getAttendanceStatus()))
                    .attendanceRemark(request.getRemark())
                    .confirmedBy(currentUser.getUserId())
                    .confirmedAt(LocalDateTime.now())
                    .build();
            
            if ("PRESENT".equals(request.getAttendanceStatus())) {
                lessonStudent.setSignInAt(LocalDateTime.now());
            }
            
            scheduleRepository.saveLessonStudent(lessonStudent);
        } else {
            existing.setAttendanceStatus(request.getAttendanceStatus());
            existing.setAttendanceRemark(request.getRemark());
            existing.setConfirmedBy(currentUser.getUserId());
            existing.setConfirmedAt(LocalDateTime.now());
            
            if ("PRESENT".equals(request.getAttendanceStatus()) && existing.getSignInAt() == null) {
                existing.setSignInAt(LocalDateTime.now());
            }
            
            scheduleRepository.saveLessonStudent(existing);
        }
        
        log.info("记录出勤: lessonId={}, studentId={}, status={}", 
                request.getLessonId(), request.getStudentId(), request.getAttendanceStatus());
    }
    
    public List<Lesson> getLessonsByTeacher(Long teacherId, LocalDate date) {
        return scheduleRepository.findByTeacherIdAndDate(teacherId, date);
    }
    
    public List<Lesson> getLessonsByClassGroup(Long classGroupId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findByClassGroupIdAndDateRange(classGroupId, startDate, endDate);
    }
    
    public List<LessonStudent> getLessonAttendance(Long lessonId) {
        return scheduleRepository.findByLessonId(lessonId);
    }
    
    public List<ScheduleSeries> listSchedules(Long teacherId) {
        return scheduleRepository.findByTeacherId(teacherId);
    }
    
    public ScheduleSeries getSchedule(Long id) {
        return scheduleRepository.findScheduleSeriesById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }
    
    @Transactional
    public void deleteSchedule(Long id) {
        ScheduleSeries series = scheduleRepository.findScheduleSeriesById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        
        scheduleRepository.deleteScheduleSeries(id);
        log.info("删除排课系列: id={}", id);
    }
    
    public List<Lesson> getChildSchedules(Long studentId, LocalDate startDate, LocalDate endDate, CurrentUser currentUser) {
        return scheduleRepository.findByStudentIdAndDateRange(studentId, startDate, endDate);
    }

    @Transactional
    public void submitRating(Long lessonId, Long studentId, java.math.BigDecimal rating, String comment) {
        LessonStudent lessonStudent = scheduleRepository.findLessonStudent(lessonId, studentId)
                .orElseGet(() -> LessonStudent.builder()
                        .lessonId(lessonId)
                        .studentId(studentId)
                        .build());

        lessonStudent.setRating(rating.intValue());
        lessonStudent.setRatingComment(comment);

        scheduleRepository.saveLessonStudent(lessonStudent);
        log.info("提交课程评分: lessonId={}, studentId={}, rating={}", lessonId, studentId, rating);
    }
}
