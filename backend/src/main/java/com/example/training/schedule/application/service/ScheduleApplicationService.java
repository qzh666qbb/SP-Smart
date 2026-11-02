package com.example.training.schedule.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.course.domain.repository.CourseRepository;
import com.example.training.hour.application.service.HourApplicationService;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.schedule.application.dto.AttendanceRequest;
import com.example.training.schedule.application.dto.ScheduleCreateRequest;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.model.ScheduleSeries;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import com.example.training.student.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Set;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ScheduleApplicationService.class);

    
    private final ScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final HourApplicationService hourApplicationService;
    private final UserRepository userRepository;
    
    @Transactional
    public ScheduleSeries createScheduleSeries(ScheduleCreateRequest request, CurrentUser currentUser) {
        Long teacherId = resolveTeacherId(request, currentUser);
        validateReferencedData(request, teacherId);
        validateTeacherAssignments(teacherId, request.getAssistantTeacherId1(), request.getAssistantTeacherId2());
        validateRecurrence(request);
        assertNoTeacherConflict(request, null);
        ScheduleSeries series = ScheduleSeries.builder()
                .seriesName(request.getSeriesName())
                .courseId(request.getCourseId())
                .classGroupId(request.getClassGroupId())
                .teacherId(teacherId)
                .assistantTeacherId1(request.getAssistantTeacherId1())
                .assistantTeacherId2(request.getAssistantTeacherId2())
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
        List<Long> selectedStudentIds = mergeStudentIds(request.getStudentIds(), classStudentIds);
        validateSelectedStudents(selectedStudentIds, currentUser, teacherId);
        scheduleRepository.replaceScheduleSeriesStudents(savedSeries.getId(), selectedStudentIds, currentUser.getUserId());

        if ("WEEKLY".equals(request.getRecurrenceType()) && request.getWeekdays() != null && !request.getWeekdays().isBlank()) {
            generateLessonsFromSeries(savedSeries, selectedStudentIds);
        } else if ("NONE".equals(request.getRecurrenceType())) {
            // 单次排课：在 startDate 上生成 1 节 lesson
            Lesson lesson = buildLesson(savedSeries, request.getStartDate());
            Lesson savedLesson = scheduleRepository.saveLesson(lesson);
            attachStudentsForLesson(savedLesson, selectedStudentIds);
            log.info("生成单次课程: lessonNo={}, date={}", lesson.getLessonNo(), request.getStartDate());
        }
        
        return savedSeries;
    }

    private Long resolveTeacherId(ScheduleCreateRequest request, CurrentUser currentUser) {
        if (currentUser != null && currentUser.isTeacher()) {
            if (currentUser.getProfileId() == null) {
                throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "当前账号未关联教师资料");
            }
            return currentUser.getProfileId();
        }
        if (request.getTeacherId() == null) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "教师ID不能为空");
        }
        return request.getTeacherId();
    }

    private void validateTeacherAssignments(Long primaryTeacherId, Long assistantTeacherId1, Long assistantTeacherId2) {
        if (assistantTeacherId1 != null && assistantTeacherId1.equals(primaryTeacherId)
                || assistantTeacherId2 != null && assistantTeacherId2.equals(primaryTeacherId)
                || assistantTeacherId1 != null && assistantTeacherId1.equals(assistantTeacherId2)) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "主教和助教不能重复选择");
        }
    }

    private List<Long> mergeStudentIds(List<Long> requestedIds, List<Long> classStudentIds) {
        return java.util.stream.Stream.concat(
                        requestedIds == null ? java.util.stream.Stream.empty() : requestedIds.stream(),
                        classStudentIds == null ? java.util.stream.Stream.empty() : classStudentIds.stream())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private void validateSelectedStudents(List<Long> studentIds, CurrentUser currentUser, Long teacherId) {
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "请至少选择一名学员");
        }
        if (currentUser != null && currentUser.isTeacher()) {
            boolean hasUnauthorizedStudent = studentIds.stream()
                    .anyMatch(studentId -> !studentRepository.existsByTeacherAndStudentId(teacherId, studentId));
            if (hasUnauthorizedStudent) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只能为本人负责的学员排课");
            }
        }
        boolean hasMissingStudent = studentIds.stream().anyMatch(id -> studentRepository.findById(id).isEmpty());
        if (hasMissingStudent) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "所选学员不存在或已删除");
        }
    }

    private void validateReferencedData(ScheduleCreateRequest request, Long teacherId) {
        com.example.training.course.domain.model.Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_VALID_ERROR, "课程不存在或已停用"));
        if (!course.isEnabled()) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "课程不存在或已停用");
        }
        if (request.getClassGroupId() != null) {
            com.example.training.course.domain.model.ClassGroup classGroup = courseRepository
                    .findClassGroupById(request.getClassGroupId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_VALID_ERROR, "班级不存在或已停用"));
            if (!classGroup.isActive() || !request.getCourseId().equals(classGroup.getCourseId())) {
                throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "班级未启用或不属于所选课程");
            }
        }
        java.util.stream.Stream.of(teacherId, request.getAssistantTeacherId1(), request.getAssistantTeacherId2())
                .filter(java.util.Objects::nonNull)
                .forEach(id -> {
                    com.example.training.auth.domain.model.TeacherProfile teacher = userRepository.findTeacherProfileById(id)
                            .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_VALID_ERROR, "教师不存在或未建立教师档案"));
                    if (!"ACTIVE".equals(teacher.getEmploymentStatus())) {
                        throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "教师已停用，不能排课");
                    }
                });
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
                .assistantTeacherId1(series.getAssistantTeacherId1())
                .assistantTeacherId2(series.getAssistantTeacherId2())
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
        generateLessonsFromSeries(series, classStudentIds, series.getStartDate());
    }

    private void generateLessonsFromSeries(ScheduleSeries series, List<Long> classStudentIds, LocalDate generationStart) {
        LocalDate currentDate = generationStart.isAfter(series.getStartDate()) ? generationStart : series.getStartDate();
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
                    if (scheduleRepository.existsLessonBySeriesIdAndDate(series.getId(), currentDate)) {
                        continue;
                    }
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

        assertCanManageLesson(lesson, currentUser);
        
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
        assertCanManageLesson(lesson, currentUser);
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
        assertCanManageLesson(lesson, currentUser);
        if (lesson.isCompleted() || lesson.isCancelled()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已完成或已取消的课程不能取消");
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

        assertCanManageLesson(lesson, currentUser);
        if (currentUser.isTeacher() && !lesson.isInProgress()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程开始后才能记录考勤");
        }
        if (currentUser.isAdmin() && !lesson.isInProgress() && !lesson.isCompleted()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只能为进行中或已完成的课程记录考勤");
        }
        if (currentUser.isParent()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime openAt = LocalDateTime.of(lesson.getLessonDate(), lesson.getStartTime()).minusMinutes(30);
            LocalDateTime closeAt = LocalDateTime.of(lesson.getLessonDate(), lesson.getEndTime());
            if (!"PRESENT".equals(request.getAttendanceStatus())) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "家长只能进行到课签到");
            }
            if (lesson.isCancelled() || lesson.isCompleted() || now.isBefore(openAt) || now.isAfter(closeAt)) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "当前不在签到时间内");
            }
        }
        
        LessonStudent existing = scheduleRepository.findLessonStudent(request.getLessonId(), request.getStudentId())
                .orElse(null);
        
        if (existing == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "学员不在本次课程名单中");
        }

        existing.setAttendanceStatus(request.getAttendanceStatus());
        existing.setAttendanceRemark(request.getRemark());
        existing.setConfirmedBy(currentUser.getUserId());
        existing.setConfirmedAt(LocalDateTime.now());

        boolean chargeableAttendance = "PRESENT".equals(request.getAttendanceStatus())
                || "LATE".equals(request.getAttendanceStatus());
        if (chargeableAttendance && existing.getSignInAt() == null) {
            existing.setSignInAt(LocalDateTime.now());
        } else if (!chargeableAttendance) {
            existing.setSignInAt(null);
        }

        LessonStudent saved = scheduleRepository.saveLessonStudent(existing);

        // 已结课课程由管理员修正考勤时，同步修正课时流水。
        if (currentUser.isAdmin() && lesson.isCompleted() && chargeableAttendance
                && !Boolean.TRUE.equals(saved.getShouldConsume())) {
            BigDecimal consumeAmount = saved.getConsumeHours() != null
                    ? saved.getConsumeHours()
                    : lesson.getConsumeHours() != null ? lesson.getConsumeHours() : BigDecimal.ONE;
            if (consumeAmount.compareTo(BigDecimal.ZERO) > 0) {
                HourTransaction transaction = hourApplicationService.consume(
                        saved.getStudentId(), consumeAmount, lesson.getId(), saved.getId(),
                        "管理员补签消课 - " + lesson.getLessonNo(), currentUser);
                saved.setShouldConsume(true);
                saved.setConsumeHours(consumeAmount);
                saved.setHourTransactionId(transaction.getId());
                scheduleRepository.saveLessonStudent(saved);
            }
        } else if (currentUser.isAdmin() && lesson.isCompleted() && !chargeableAttendance
                && Boolean.TRUE.equals(saved.getShouldConsume()) && saved.getHourTransactionId() != null) {
            hourApplicationService.refundByTransaction(saved.getHourTransactionId(),
                    "管理员修正考勤退回 - " + lesson.getLessonNo(), currentUser);
            saved.setShouldConsume(false);
            saved.setHourTransactionId(null);
            scheduleRepository.saveLessonStudent(saved);
        }
        
        log.info("记录出勤: lessonId={}, studentId={}, status={}", 
                request.getLessonId(), request.getStudentId(), request.getAttendanceStatus());
    }
    
    public List<Lesson> getLessonsByTeacher(Long teacherId, LocalDate date) {
        return enrichLessons(scheduleRepository.findByTeacherIdAndDate(teacherId, date));
    }

    public List<Lesson> getLessonsByTeacher(Long teacherId, LocalDate startDate, LocalDate endDate) {
        return enrichLessons(scheduleRepository.findByTeacherIdAndDateRange(teacherId, startDate, endDate));
    }
    
    public List<Lesson> getLessonsByClassGroup(Long classGroupId, LocalDate startDate, LocalDate endDate) {
        return enrichLessons(scheduleRepository.findByClassGroupIdAndDateRange(classGroupId, startDate, endDate));
    }
    
    public List<LessonStudent> getLessonAttendance(Long lessonId, CurrentUser currentUser) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
        assertCanManageLesson(lesson, currentUser);
        List<LessonStudent> attendance = scheduleRepository.findByLessonId(lessonId);
        for (LessonStudent item : attendance) {
            studentRepository.findById(item.getStudentId()).ifPresent(student -> {
                item.setStudentName(student.getName());
                item.setStudentNo(student.getStudentNo());
            });
        }
        return attendance;
    }

    private void assertCanManageLesson(Lesson lesson, CurrentUser currentUser) {
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED);
        }
        if (currentUser.isTeacher()
                && (currentUser.getProfileId() == null
                || !(currentUser.getProfileId().equals(lesson.getTeacherId())
                || currentUser.getProfileId().equals(lesson.getAssistantTeacherId1())
                || currentUser.getProfileId().equals(lesson.getAssistantTeacherId2())))) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只能操作本人负责的课程");
        }
    }
    
    public List<ScheduleSeries> listSchedules(Long teacherId) {
        return listSchedulesPage(teacherId, null, null, 1, Integer.MAX_VALUE).getList();
    }

    public PageResult<ScheduleSeries> listSchedulesPage(Long teacherId, String keyword, LocalDate date, int pageNum, int pageSize) {
        List<ScheduleSeries> all = scheduleRepository.findByTeacherId(teacherId);
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        List<ScheduleSeries> filtered = all.stream()
                .filter(s -> kw == null || kw.isEmpty()
                        || contains(s.getSeriesName(), kw)
                        || contains(s.getRecurrenceType(), kw)
                        || contains(String.valueOf(s.getCourseId()), kw)
                        || contains(String.valueOf(s.getTeacherId()), kw))
                .filter(s -> matchesDate(s, date))
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
    
    public ScheduleSeries getSchedule(Long id, CurrentUser currentUser) {
        ScheduleSeries series = scheduleRepository.findScheduleSeriesById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (currentUser != null && currentUser.isTeacher()) {
            Long teacherId = currentUser.getProfileId();
            if (teacherId == null || !(teacherId.equals(series.getTeacherId())
                    || teacherId.equals(series.getAssistantTeacherId1())
                    || teacherId.equals(series.getAssistantTeacherId2()))) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "只能查看本人负责的排课");
            }
        }
        List<Long> classStudentIds = series.getClassGroupId() == null
                ? java.util.Collections.emptyList()
                : courseRepository.findStudentIdsByClassGroupId(series.getClassGroupId());
        series.setStudentIds(mergeStudentIds(scheduleRepository.findStudentIdsByScheduleSeriesId(id), classStudentIds));
        return series;
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

        Long resolvedTeacherId = resolveTeacherId(request, currentUser);
        validateReferencedData(request, resolvedTeacherId);
        validateRecurrence(request);
        assertNoTeacherConflict(request, id);
        if (request.getSeriesName() != null) series.setSeriesName(request.getSeriesName());
        if (request.getCourseId() != null) series.setCourseId(request.getCourseId());
        if (request.getClassGroupId() != null) series.setClassGroupId(request.getClassGroupId());
        series.setTeacherId(resolvedTeacherId);
        validateTeacherAssignments(series.getTeacherId(), request.getAssistantTeacherId1(), request.getAssistantTeacherId2());
        series.setAssistantTeacherId1(request.getAssistantTeacherId1());
        series.setAssistantTeacherId2(request.getAssistantTeacherId2());
        if (request.getRecurrenceType() != null) series.setRecurrenceType(request.getRecurrenceType());
        if (request.getWeekdays() != null) series.setWeekdays(request.getWeekdays());
        if (request.getStartDate() != null) series.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) series.setEndDate(request.getEndDate());
        if (request.getStartTime() != null) series.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) series.setEndTime(request.getEndTime());
        if (request.getConsumeHours() != null) series.setConsumeHours(request.getConsumeHours());
        series.setUpdatedBy(currentUser.getUserId());

        ScheduleSeries saved = scheduleRepository.saveScheduleSeries(series);
        List<Long> classStudentIds = saved.getClassGroupId() != null
                ? courseRepository.findStudentIdsByClassGroupId(saved.getClassGroupId())
                : java.util.Collections.emptyList();
        List<Long> selectedStudentIds = mergeStudentIds(request.getStudentIds(), classStudentIds);
        validateSelectedStudents(selectedStudentIds, currentUser, saved.getTeacherId());
        scheduleRepository.replaceScheduleSeriesStudents(saved.getId(), selectedStudentIds, currentUser.getUserId());
        LocalDate rebuildFrom = LocalDate.now().isAfter(saved.getStartDate()) ? LocalDate.now() : saved.getStartDate();
        scheduleRepository.softDeletePendingLessonsBySeries(saved.getId(), rebuildFrom);
        if ("WEEKLY".equals(saved.getRecurrenceType())) {
            generateLessonsFromSeries(saved, selectedStudentIds, rebuildFrom);
        } else if (!rebuildFrom.isAfter(saved.getStartDate())) {
            Lesson savedLesson = scheduleRepository.saveLesson(buildLesson(saved, saved.getStartDate()));
            attachStudentsForLesson(savedLesson, selectedStudentIds);
        }
        log.info("更新排课系列: id={}, name={}", id, saved.getSeriesName());
        return saved;
    }
    
    public List<Lesson> getChildSchedules(Long studentId, LocalDate startDate, LocalDate endDate, CurrentUser currentUser) {
        List<Lesson> lessons = enrichLessons(scheduleRepository.findByStudentIdAndDateRange(studentId, startDate, endDate));
        for (Lesson lesson : lessons) {
            scheduleRepository.findLessonStudent(lesson.getId(), studentId).ifPresent(item ->
                    lesson.setStudentAttendance(java.util.Map.of(studentId, item.getAttendanceStatus())));
        }
        return lessons;
    }

    @Transactional
    public void submitRating(Long lessonId, Long studentId, BigDecimal rating, String comment) {
        Lesson lesson = scheduleRepository.findLessonById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        if (!lesson.isCompleted()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程完成后才能评分");
        }
        LessonStudent lessonStudent = scheduleRepository.findLessonStudent(lessonId, studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "学员不在本次课程名单中"));

        lessonStudent.setRating(rating.intValue());
        lessonStudent.setRatingComment(comment);

        scheduleRepository.saveLessonStudent(lessonStudent);
        log.info("提交课程评分: lessonId={}, studentId={}, rating={}", lessonId, studentId, rating);
    }

    private void validateRecurrence(ScheduleCreateRequest request) {
        if (!"WEEKLY".equals(request.getRecurrenceType())) return;
        Set<Integer> days = new HashSet<>();
        for (String token : request.getWeekdays().split(",")) {
            int day = parseWeekday(token);
            if (day < 1 || !days.add(day)) {
                throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "星期参数无效或重复");
            }
        }
    }

    private void assertNoTeacherConflict(ScheduleCreateRequest request, Long excludedSeriesId) {
        List<Long> teacherIds = java.util.stream.Stream.of(
                        request.getTeacherId(), request.getAssistantTeacherId1(), request.getAssistantTeacherId2())
                .filter(java.util.Objects::nonNull).distinct().toList();
        LocalDate end = "WEEKLY".equals(request.getRecurrenceType()) ? request.getEndDate() : request.getStartDate();
        Set<Integer> weeklyDays = new HashSet<>();
        if ("WEEKLY".equals(request.getRecurrenceType())) {
            for (String token : request.getWeekdays().split(",")) weeklyDays.add(parseWeekday(token));
        }
        for (Long teacherId : teacherIds) {
            for (Lesson lesson : scheduleRepository.findByTeacherIdAndDateRange(teacherId, request.getStartDate(), end)) {
                if (excludedSeriesId != null && excludedSeriesId.equals(lesson.getScheduleSeriesId())) continue;
                if (lesson.isCancelled()) continue;
                if (!weeklyDays.isEmpty() && !weeklyDays.contains(lesson.getLessonDate().getDayOfWeek().getValue())) continue;
                if (request.getStartTime().isBefore(lesson.getEndTime())
                        && request.getEndTime().isAfter(lesson.getStartTime())) {
                    throw new BusinessException(ErrorCode.PARAM_VALID_ERROR,
                            "教师在" + lesson.getLessonDate() + "已有时间冲突的课程");
                }
            }
        }
    }

    private boolean matchesDate(ScheduleSeries series, LocalDate date) {
        if (date == null) return true;
        if (date.isBefore(series.getStartDate()) || series.getEndDate() != null && date.isAfter(series.getEndDate())) return false;
        if ("NONE".equals(series.getRecurrenceType())) return date.equals(series.getStartDate());
        if (!"WEEKLY".equals(series.getRecurrenceType()) || series.getWeekdays() == null) return false;
        int wanted = date.getDayOfWeek().getValue();
        for (String token : series.getWeekdays().split(",")) if (parseWeekday(token) == wanted) return true;
        return false;
    }

    private List<Lesson> enrichLessons(List<Lesson> lessons) {
        java.util.Map<Long, String> courses = new java.util.HashMap<>();
        java.util.Map<Long, String> teachers = new java.util.HashMap<>();
        for (Lesson lesson : lessons) {
            if (lesson.getCourseId() != null) {
                lesson.setCourseName(courses.computeIfAbsent(lesson.getCourseId(), id ->
                        courseRepository.findById(id).map(c -> c.getCourseName()).orElse(null)));
            }
            if (lesson.getTeacherId() != null) {
                lesson.setTeacherName(teachers.computeIfAbsent(lesson.getTeacherId(), id ->
                        userRepository.findTeacherProfileById(id).map(p -> p.getName()).orElse(null)));
            }
        }
        return lessons;
    }

    @Transactional
    public void backfillMissingLessons() {
        LocalDate earliest = LocalDate.now().withDayOfYear(1);
        for (ScheduleSeries series : scheduleRepository.findByTeacherId(null)) {
            if (!series.isActive() || series.getStartDate() == null) continue;
            LocalDate start = series.getStartDate().isAfter(earliest) ? series.getStartDate() : earliest;
            LocalDate end = series.getEndDate() != null ? series.getEndDate() : LocalDate.now().plusYears(1);
            if (start.isAfter(end)) continue;
            List<Long> classStudentIds = series.getClassGroupId() == null
                    ? java.util.Collections.emptyList()
                    : courseRepository.findStudentIdsByClassGroupId(series.getClassGroupId());
            List<Long> studentIds = mergeStudentIds(
                    scheduleRepository.findStudentIdsByScheduleSeriesId(series.getId()), classStudentIds);
            if ("WEEKLY".equals(series.getRecurrenceType())
                    && series.getWeekdays() != null && !series.getWeekdays().isBlank()) {
                generateLessonsFromSeries(series, studentIds, start);
            } else if ("NONE".equals(series.getRecurrenceType())
                    && !scheduleRepository.existsLessonBySeriesIdAndDate(series.getId(), series.getStartDate())) {
                Lesson lesson = scheduleRepository.saveLesson(buildLesson(series, series.getStartDate()));
                attachStudentsForLesson(lesson, studentIds);
            }
        }
    }
}
