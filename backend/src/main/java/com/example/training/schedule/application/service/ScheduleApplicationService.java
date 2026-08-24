package com.example.training.schedule.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.course.domain.repository.CourseRepository;
import com.example.training.schedule.application.dto.AttendanceRequest;
import com.example.training.schedule.application.dto.ScheduleCreateRequest;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.model.ScheduleSeries;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleApplicationService.class);

    
    private final ScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    
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
        
        List<Long> classStudentIds = savedSeries.getClassGroupId() != null
                ? courseRepository.findStudentIdsByClassGroupId(savedSeries.getClassGroupId())
                : java.util.Collections.emptyList();

        if ("WEEKLY".equals(request.getRecurrenceType()) && request.getWeekdays() != null && !request.getWeekdays().isBlank()) {
            generateLessonsFromSeries(savedSeries, classStudentIds);
        } else if ("NONE".equals(request.getRecurrenceType())) {
            // 单次排课：在 startDate 上生成 1 节 lesson
            Lesson lesson = buildLesson(savedSeries, request.getStartDate());
            Lesson savedLesson = scheduleRepository.saveLesson(lesson);
            attachStudentsForLesson(savedLesson, classStudentIds);
            log.info("生成单次课程: lessonNo={}, date={}", lesson.getLessonNo(), request.getStartDate());
        }
        
        return savedSeries;
    }

    private static int parseWeekday(String token) {
        if (token == null) return -1;
        String t = token.trim().toUpperCase(Locale.ROOT);
        if (t.isEmpty()) return -1;
        // 数字形式 1-7 (Java DayOfWeek MONDAY=1 ... SUNDAY=7)
        try {
            int n = Integer.parseInt(t);
            if (n >= 1 && n <= 7) return n;
        } catch (NumberFormatException ignore) { /* fallthrough to enum */ }
        try {
            // MONDAY ... SUNDAY
            return DayOfWeek.valueOf(t).getValue();
        } catch (IllegalArgumentException ignore) { /* fallthrough */ }
        // 简名 MON/TUE/WED/THU/FRI/SAT/SUN
        return switch (t) {
            case "MON" -> 1; case "TUE" -> 2; case "WED" -> 3; case "THU" -> 4;
            case "FRI" -> 5; case "SAT" -> 6; case "SUN" -> 7;
            default -> -1;
        };
    }

    private Lesson buildLesson(ScheduleSeries series, LocalDate date) {
        String lessonNo = "LSN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return Lesson.builder()
                .lessonNo(lessonNo)
                .scheduleSeriesId(series.getId())
                .courseId(series.getCourseId())
                .classGroupId(series.getClassGroupId())
                .teacherId(series.getTeacherId())
                .lessonDate(date)
                .startTime(series.getStartTime())
                .endTime(series.getEndTime())
                .status("PENDING")
                .sourceType("SERIES")
                .consumeHours(series.getConsumeHours())
                .createdBy(series.getCreatedBy())
                .build();
    }
    
    private void generateLessonsFromSeries(ScheduleSeries series, List<Long> classStudentIds) {
        LocalDate currentDate = series.getStartDate();
        LocalDate endDate = series.getEndDate() != null ? series.getEndDate() : LocalDate.now().plusYears(1);
        
        List<Integer> days = new ArrayList<>();
        for (String token : (series.getWeekdays() == null ? "" : series.getWeekdays()).split(",")) {
            int d = parseWeekday(token);
            if (d >= 1) days.add(d);
        }
        if (days.isEmpty()) return;
        
        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            for (int d : days) {
                if (dayOfWeek == d) {
                    Lesson lesson = buildLesson(series, currentDate);
                    Lesson savedLesson = scheduleRepository.saveLesson(lesson);
                    attachStudentsForLesson(savedLesson, classStudentIds);
                    log.info("生成周循环课程: lessonNo={}, date={}, weekday={}", lesson.getLessonNo(), currentDate, d);
                }
            }
            currentDate = currentDate.plus(1, ChronoUnit.DAYS);
        }
    }

    private void attachStudentsForLesson(Lesson lesson, List<Long> studentIds) {
        if (lesson == null || lesson.getId() == null || studentIds == null || studentIds.isEmpty()) {
            return;
        }
        BigDecimal lessonHours = lesson.getConsumeHours() != null ? lesson.getConsumeHours() : BigDecimal.ZERO;
        for (Long sid : studentIds) {
            if (sid == null) continue;
            // 幂等：已存在则跳过，避免并发重建产生重复记录
            if (scheduleRepository.findLessonStudent(lesson.getId(), sid).isPresent()) {
                continue;
            }
            LessonStudent ls = LessonStudent.builder()
                    .lessonId(lesson.getId())
                    .studentId(sid)
                    .attendanceStatus("PENDING")
                    .shouldConsume(false)
                    .consumeHours(lessonHours)
                    .build();
            scheduleRepository.saveLessonStudent(ls);
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
                    .shouldConsume(false)
                    .consumeHours(lesson.getConsumeHours() != null ? lesson.getConsumeHours() : BigDecimal.ZERO)
                    .attendanceRemark(request.getRemark())
                    .confirmedBy(currentUser.getUserId())
                    .confirmedAt(LocalDateTime.now())
                    .build();
            
            if ("PRESENT".equals(request.getAttendanceStatus())) {
                lessonStudent.setSignInAt(LocalDateTime.now());
            }

            try {
                scheduleRepository.saveLessonStudent(lessonStudent);
            } catch (DataIntegrityViolationException dup) {
                // 并发考勤：UNIQUE(lesson_id, student_id) 冲突 → 说明已有其他线程写入，改为幂等更新
                log.warn("考勤记录并发插入冲突，改为幂等更新: lessonId={}, studentId={}, cause={}",
                        request.getLessonId(), request.getStudentId(),
                        dup.getCause() != null ? dup.getCause().getMessage() : dup.getMessage());
                LessonStudent concurrentRow = scheduleRepository.findLessonStudent(
                        request.getLessonId(), request.getStudentId()).orElse(null);
                if (concurrentRow == null) {
                    throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "考勤记录写入冲突，请重试");
                }
                concurrentRow.setAttendanceStatus(request.getAttendanceStatus());
                concurrentRow.setAttendanceRemark(request.getRemark());
                concurrentRow.setConfirmedBy(currentUser.getUserId());
                concurrentRow.setConfirmedAt(LocalDateTime.now());
                if ("PRESENT".equals(request.getAttendanceStatus()) && concurrentRow.getSignInAt() == null) {
                    concurrentRow.setSignInAt(LocalDateTime.now());
                }
                scheduleRepository.saveLessonStudent(concurrentRow);
            }
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
        return listSchedulesPage(teacherId, null, 1, Integer.MAX_VALUE).getList();
    }

    public PageResult<ScheduleSeries> listSchedulesPage(Long teacherId, String keyword, int pageNum, int pageSize) {
        List<ScheduleSeries> all = scheduleRepository.findByTeacherId(teacherId);
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        List<ScheduleSeries> filtered = all.stream()
                .filter(s -> kw == null || kw.isEmpty()
                        || contains(s.getSeriesName(), kw)
                        || contains(s.getRecurrenceType(), kw)
                        || contains(String.valueOf(s.getCourseId()), kw)
                        || contains(String.valueOf(s.getTeacherId()), kw))
                .collect(java.util.stream.Collectors.toList());
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(from + pageSize, filtered.size());
        List<ScheduleSeries> page = from >= filtered.size() ? new ArrayList<>() : new ArrayList<>(filtered.subList(from, to));
        return PageResult.of(page, filtered.size(), pageNum, pageSize);
    }

    private static boolean contains(String src, String lowerKw) {
        if (src == null || lowerKw == null) return false;
        return src.toLowerCase().contains(lowerKw);
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

    @Transactional
    public ScheduleSeries updateSchedule(Long id, ScheduleCreateRequest request, CurrentUser currentUser) {
        ScheduleSeries series = scheduleRepository.findScheduleSeriesById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (request.getSeriesName() != null) series.setSeriesName(request.getSeriesName());
        if (request.getCourseId() != null) series.setCourseId(request.getCourseId());
        if (request.getClassGroupId() != null) series.setClassGroupId(request.getClassGroupId());
        if (request.getTeacherId() != null) series.setTeacherId(request.getTeacherId());
        if (request.getRecurrenceType() != null) series.setRecurrenceType(request.getRecurrenceType());
        if (request.getWeekdays() != null) series.setWeekdays(request.getWeekdays());
        if (request.getStartDate() != null) series.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) series.setEndDate(request.getEndDate());
        if (request.getStartTime() != null) series.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) series.setEndTime(request.getEndTime());
        if (request.getConsumeHours() != null) series.setConsumeHours(request.getConsumeHours());
        series.setUpdatedBy(currentUser.getUserId());

        ScheduleSeries saved = scheduleRepository.saveScheduleSeries(series);
        log.info("更新排课系列: id={}, name={}", id, saved.getSeriesName());
        return saved;
    }
    
    public List<Lesson> getChildSchedules(Long studentId, LocalDate startDate, LocalDate endDate, CurrentUser currentUser) {
        return scheduleRepository.findByStudentIdAndDateRange(studentId, startDate, endDate);
    }

    @Transactional
    public void submitRating(Long lessonId, Long studentId, BigDecimal rating, String comment) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        LessonStudent lessonStudent = scheduleRepository.findLessonStudent(lessonId, studentId)
                .orElseGet(() -> LessonStudent.builder()
                        .lessonId(lessonId)
                        .studentId(studentId)
                        .consumeHours(lesson.getConsumeHours() != null ? lesson.getConsumeHours() : BigDecimal.ZERO)
                        .build());

        lessonStudent.setRating(rating.intValue());
        lessonStudent.setRatingComment(comment);

        scheduleRepository.saveLessonStudent(lessonStudent);
        log.info("提交课程评分: lessonId={}, studentId={}, rating={}", lessonId, studentId, rating);
    }
}
