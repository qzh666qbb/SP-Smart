package com.example.training.schedule.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.model.ScheduleSeries;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import com.example.training.schedule.infrastructure.persistence.LessonEntity;
import com.example.training.schedule.infrastructure.persistence.LessonMapper;
import com.example.training.schedule.infrastructure.persistence.LessonStudentEntity;
import com.example.training.schedule.infrastructure.persistence.LessonStudentMapper;
import com.example.training.schedule.infrastructure.persistence.ScheduleSeriesEntity;
import com.example.training.schedule.infrastructure.persistence.ScheduleSeriesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepository {
    
    private final ScheduleSeriesMapper scheduleSeriesMapper;
    private final LessonMapper lessonMapper;
    private final LessonStudentMapper lessonStudentMapper;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public ScheduleSeries saveScheduleSeries(ScheduleSeries series) {
        ScheduleSeriesEntity entity = convertToEntity(series);
        if (series.getId() == null) {
            scheduleSeriesMapper.insert(entity);
        } else {
            scheduleSeriesMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<ScheduleSeries> findScheduleSeriesById(Long id) {
        ScheduleSeriesEntity entity = scheduleSeriesMapper.selectOne(
                new LambdaQueryWrapper<ScheduleSeriesEntity>()
                        .eq(ScheduleSeriesEntity::getId, id)
                        .isNull(ScheduleSeriesEntity::getDeletedAt));
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<ScheduleSeries> findByTeacherId(Long teacherId) {
        LambdaQueryWrapper<ScheduleSeriesEntity> wrapper = new LambdaQueryWrapper<ScheduleSeriesEntity>()
                .isNull(ScheduleSeriesEntity::getDeletedAt);
        if (teacherId != null) {
            wrapper.and(w -> w.eq(ScheduleSeriesEntity::getTeacherId, teacherId)
                    .or().eq(ScheduleSeriesEntity::getAssistantTeacherId1, teacherId)
                    .or().eq(ScheduleSeriesEntity::getAssistantTeacherId2, teacherId));
        }
        wrapper.orderByDesc(ScheduleSeriesEntity::getStartDate)
                .orderByAsc(ScheduleSeriesEntity::getStartTime)
                .orderByDesc(ScheduleSeriesEntity::getId);
        List<ScheduleSeriesEntity> entities = scheduleSeriesMapper.selectList(wrapper);
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<ScheduleSeries> findByClassGroupId(Long classGroupId) {
        List<ScheduleSeriesEntity> entities = scheduleSeriesMapper.selectList(
                new LambdaQueryWrapper<ScheduleSeriesEntity>()
                        .eq(ScheduleSeriesEntity::getClassGroupId, classGroupId)
                        .isNull(ScheduleSeriesEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public Lesson saveLesson(Lesson lesson) {
        LessonEntity entity = convertToEntity(lesson);
        if (lesson.getId() == null) {
            lessonMapper.insert(entity);
            if (entity.getId() == null) {
                LessonEntity found = lessonMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LessonEntity>()
                        .eq(LessonEntity::getLessonNo, lesson.getLessonNo())
                        .orderByDesc(LessonEntity::getId)
                        .last("LIMIT 1"));
                if (found != null) {
                    entity.setId(found.getId());
                }
            }
        } else {
            int rows = lessonMapper.updateWithVersion(entity);
            if (rows == 0) {
                throw new com.example.training.common.exception.BusinessException(
                        com.example.training.common.exception.ErrorCode.OPERATION_NOT_ALLOWED, "课程已更新，请刷新后重试");
            }
        }
        LessonEntity saved = lessonMapper.selectById(entity.getId());
        if (saved == null) {
            throw new com.baomidou.mybatisplus.core.exceptions.MybatisPlusException("课程读取失败: id=" + entity.getId());
        }
        return convertToDomain(saved);
    }
    
    @Override
    public Optional<Lesson> findLessonById(Long id) {
        LessonEntity entity = lessonMapper.selectOne(
                new LambdaQueryWrapper<LessonEntity>()
                        .eq(LessonEntity::getId, id)
                        .isNull(LessonEntity::getDeletedAt));
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<Lesson> findByLessonNo(String lessonNo) {
        LessonEntity entity = lessonMapper.selectOne(
                new LambdaQueryWrapper<LessonEntity>()
                        .eq(LessonEntity::getLessonNo, lessonNo)
                        .isNull(LessonEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<Lesson> findByTeacherIdAndDate(Long teacherId, LocalDate date) {
        return findByTeacherIdAndDateRange(teacherId, date, date);
    }

    @Override
    public List<Lesson> findByTeacherIdAndDateRange(Long teacherId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<LessonEntity> wrapper = new LambdaQueryWrapper<LessonEntity>()
                .ge(startDate != null, LessonEntity::getLessonDate, startDate)
                .le(endDate != null, LessonEntity::getLessonDate, endDate)
                .isNull(LessonEntity::getDeletedAt);
        if (teacherId != null) {
            wrapper.and(w -> w.eq(LessonEntity::getTeacherId, teacherId)
                    .or().eq(LessonEntity::getAssistantTeacherId1, teacherId)
                    .or().eq(LessonEntity::getAssistantTeacherId2, teacherId));
        }
        wrapper.orderByAsc(LessonEntity::getLessonDate)
                .orderByAsc(LessonEntity::getStartTime);
        List<LessonEntity> entities = lessonMapper.selectList(wrapper);
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Lesson> findByClassGroupIdAndDateRange(Long classGroupId, LocalDate startDate, LocalDate endDate) {
        List<LessonEntity> entities = lessonMapper.selectList(
                new LambdaQueryWrapper<LessonEntity>()
                        .eq(LessonEntity::getClassGroupId, classGroupId)
                        .ge(LessonEntity::getLessonDate, startDate)
                        .le(LessonEntity::getLessonDate, endDate)
                        .isNull(LessonEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Lesson> findByStudentIdAndDateRange(Long studentId, LocalDate startDate, LocalDate endDate) {
        List<LessonStudentEntity> lessonStudents = lessonStudentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LessonStudentEntity>()
                        .eq(LessonStudentEntity::getStudentId, studentId)
        );
        if (lessonStudents == null || lessonStudents.isEmpty()) {
            return List.of();
        }
        List<Long> lessonIds = lessonStudents.stream()
                .map(LessonStudentEntity::getLessonId)
                .distinct()
                .collect(Collectors.toList());
        List<LessonEntity> lessons = lessonMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LessonEntity>()
                        .in(LessonEntity::getId, lessonIds)
                        .ge(LessonEntity::getLessonDate, startDate)
                        .le(LessonEntity::getLessonDate, endDate)
                        .isNull(LessonEntity::getDeletedAt)
                        .orderByDesc(LessonEntity::getLessonDate)
        );
        return lessons.stream().map(this::convertToDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsLessonBySeriesIdAndDate(Long seriesId, LocalDate date) {
        Long count = lessonMapper.selectCount(
                new LambdaQueryWrapper<LessonEntity>()
                        .eq(LessonEntity::getScheduleSeriesId, seriesId)
                        .eq(LessonEntity::getLessonDate, date)
                        .isNull(LessonEntity::getDeletedAt));
        return count != null && count > 0;
    }

    @Override
    public List<Long> findStudentIdsByScheduleSeriesId(Long seriesId) {
        List<Long> persistedIds = jdbcTemplate.queryForList(
                "SELECT student_id FROM schedule_series_student WHERE schedule_series_id = ?",
                Long.class, seriesId);
        // 系列名单是当前配置；历史已上课名单不能重新加入已移除的学员。
        if (!persistedIds.isEmpty()) return persistedIds;
        List<Long> lessonIds = lessonMapper.selectList(
                        new LambdaQueryWrapper<LessonEntity>()
                                .eq(LessonEntity::getScheduleSeriesId, seriesId)
                                .isNull(LessonEntity::getDeletedAt))
                .stream().map(LessonEntity::getId).toList();
        if (lessonIds.isEmpty()) return persistedIds;
        List<Long> lessonStudentIds = lessonStudentMapper.selectList(
                        new LambdaQueryWrapper<LessonStudentEntity>()
                                .in(LessonStudentEntity::getLessonId, lessonIds))
                .stream().map(LessonStudentEntity::getStudentId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        return java.util.stream.Stream.concat(persistedIds.stream(), lessonStudentIds.stream())
                .filter(java.util.Objects::nonNull).distinct().toList();
    }

    @Override
    public void replaceScheduleSeriesStudents(Long seriesId, List<Long> studentIds, Long createdBy) {
        jdbcTemplate.update("DELETE FROM schedule_series_student WHERE schedule_series_id = ?", seriesId);
        if (studentIds == null || studentIds.isEmpty()) return;
        jdbcTemplate.batchUpdate(
                "INSERT INTO schedule_series_student(schedule_series_id, student_id, created_by) VALUES (?, ?, ?)",
                studentIds,
                studentIds.size(),
                (ps, studentId) -> {
                    ps.setLong(1, seriesId);
                    ps.setLong(2, studentId);
                    if (createdBy == null) ps.setNull(3, java.sql.Types.BIGINT);
                    else ps.setLong(3, createdBy);
                });
    }
    
    @Override
    public LessonStudent saveLessonStudent(LessonStudent lessonStudent) {
        LessonStudentEntity entity = convertToEntity(lessonStudent);
        if (lessonStudent.getId() == null) {
            lessonStudentMapper.insert(entity);
        } else {
            lessonStudentMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<LessonStudent> findLessonStudent(Long lessonId, Long studentId) {
        LessonStudentEntity entity = lessonStudentMapper.selectOne(
                new LambdaQueryWrapper<LessonStudentEntity>()
                        .eq(LessonStudentEntity::getLessonId, lessonId)
                        .eq(LessonStudentEntity::getStudentId, studentId)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<LessonStudent> findByLessonId(Long lessonId) {
        List<LessonStudentEntity> entities = lessonStudentMapper.selectList(
                new LambdaQueryWrapper<LessonStudentEntity>()
                        .eq(LessonStudentEntity::getLessonId, lessonId)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<LessonStudent> findByStudentId(Long studentId) {
        List<LessonStudentEntity> entities = lessonStudentMapper.selectList(
                new LambdaQueryWrapper<LessonStudentEntity>()
                        .eq(LessonStudentEntity::getStudentId, studentId)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    private ScheduleSeriesEntity convertToEntity(ScheduleSeries series) {
        return ScheduleSeriesEntity.builder()
                .id(series.getId())
                .seriesName(series.getSeriesName())
                .courseId(series.getCourseId())
                .classGroupId(series.getClassGroupId())
                .teacherId(series.getTeacherId())
                .assistantTeacherId1(series.getAssistantTeacherId1())
                .assistantTeacherId2(series.getAssistantTeacherId2())
                .recurrenceType(series.getRecurrenceType())
                .weekdays(series.getWeekdays())
                .startDate(series.getStartDate())
                .endDate(series.getEndDate())
                .startTime(series.getStartTime())
                .endTime(series.getEndTime())
                .consumeHours(series.getConsumeHours())
                .status(series.getStatus())
                .createdBy(series.getCreatedBy())
                .createdAt(series.getCreatedAt())
                .updatedBy(series.getUpdatedBy())
                .updatedAt(series.getUpdatedAt())
                .deletedAt(series.getDeletedAt())
                .build();
    }
    
    private ScheduleSeries convertToDomain(ScheduleSeriesEntity entity) {
        return ScheduleSeries.builder()
                .id(entity.getId())
                .seriesName(entity.getSeriesName())
                .courseId(entity.getCourseId())
                .classGroupId(entity.getClassGroupId())
                .teacherId(entity.getTeacherId())
                .assistantTeacherId1(entity.getAssistantTeacherId1())
                .assistantTeacherId2(entity.getAssistantTeacherId2())
                .recurrenceType(entity.getRecurrenceType())
                .weekdays(entity.getWeekdays())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .consumeHours(entity.getConsumeHours())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    private LessonEntity convertToEntity(Lesson lesson) {
        return LessonEntity.builder()
                .id(lesson.getId())
                .lessonNo(lesson.getLessonNo())
                .scheduleSeriesId(lesson.getScheduleSeriesId())
                .courseId(lesson.getCourseId())
                .classGroupId(lesson.getClassGroupId())
                .teacherId(lesson.getTeacherId())
                .assistantTeacherId1(lesson.getAssistantTeacherId1())
                .assistantTeacherId2(lesson.getAssistantTeacherId2())
                .lessonDate(lesson.getLessonDate())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .status(lesson.getStatus())
                .location(lesson.getLocation())
                .cancelReason(lesson.getCancelReason())
                .completedAt(lesson.getCompletedAt())
                .completedBy(lesson.getCompletedBy())
                .sourceType(lesson.getSourceType())
                .consumeHours(lesson.getConsumeHours())
                .version(lesson.getVersion())
                .createdBy(lesson.getCreatedBy())
                .createdAt(lesson.getCreatedAt())
                .updatedBy(lesson.getUpdatedBy())
                .updatedAt(lesson.getUpdatedAt())
                .deletedAt(lesson.getDeletedAt())
                .build();
    }

    private Lesson convertToDomain(LessonEntity entity) {
        return Lesson.builder()
                .id(entity.getId())
                .lessonNo(entity.getLessonNo())
                .scheduleSeriesId(entity.getScheduleSeriesId())
                .courseId(entity.getCourseId())
                .classGroupId(entity.getClassGroupId())
                .teacherId(entity.getTeacherId())
                .assistantTeacherId1(entity.getAssistantTeacherId1())
                .assistantTeacherId2(entity.getAssistantTeacherId2())
                .lessonDate(entity.getLessonDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .status(entity.getStatus())
                .location(entity.getLocation())
                .cancelReason(entity.getCancelReason())
                .completedAt(entity.getCompletedAt())
                .completedBy(entity.getCompletedBy())
                .sourceType(entity.getSourceType())
                .consumeHours(entity.getConsumeHours())
                .version(entity.getVersion())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    private LessonStudentEntity convertToEntity(LessonStudent lessonStudent) {
        return LessonStudentEntity.builder()
                .id(lessonStudent.getId())
                .lessonId(lessonStudent.getLessonId())
                .studentId(lessonStudent.getStudentId())
                .attendanceStatus(lessonStudent.getAttendanceStatus())
                .signInAt(lessonStudent.getSignInAt())
                .consumeHours(lessonStudent.getConsumeHours())
                .shouldConsume(lessonStudent.getShouldConsume())
                .hourTransactionId(lessonStudent.getHourTransactionId())
                .attendanceRemark(lessonStudent.getAttendanceRemark())
                .confirmedBy(lessonStudent.getConfirmedBy())
                .confirmedAt(lessonStudent.getConfirmedAt())
                .rating(lessonStudent.getRating())
                .ratingComment(lessonStudent.getRatingComment())
                .createdAt(lessonStudent.getCreatedAt())
                .updatedAt(lessonStudent.getUpdatedAt())
                .build();
    }

    private LessonStudent convertToDomain(LessonStudentEntity entity) {
        return LessonStudent.builder()
                .id(entity.getId())
                .lessonId(entity.getLessonId())
                .studentId(entity.getStudentId())
                .attendanceStatus(entity.getAttendanceStatus())
                .signInAt(entity.getSignInAt())
                .consumeHours(entity.getConsumeHours())
                .shouldConsume(entity.getShouldConsume())
                .hourTransactionId(entity.getHourTransactionId())
                .attendanceRemark(entity.getAttendanceRemark())
                .confirmedBy(entity.getConfirmedBy())
                .confirmedAt(entity.getConfirmedAt())
                .rating(entity.getRating())
                .ratingComment(entity.getRatingComment())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    @Override
    public void deleteScheduleSeries(Long id) {
        LocalDateTime now = LocalDateTime.now();
        scheduleSeriesMapper.update(null,
                new LambdaUpdateWrapper<ScheduleSeriesEntity>()
                        .eq(ScheduleSeriesEntity::getId, id)
                        .isNull(ScheduleSeriesEntity::getDeletedAt)
                        .set(ScheduleSeriesEntity::getStatus, "DELETED")
                        .set(ScheduleSeriesEntity::getDeletedAt, now));
        lessonMapper.update(null,
                new LambdaUpdateWrapper<LessonEntity>()
                        .eq(LessonEntity::getScheduleSeriesId, id)
                        .in(LessonEntity::getStatus, "PENDING", "IN_PROGRESS")
                        .isNull(LessonEntity::getDeletedAt)
                        .set(LessonEntity::getStatus, "CANCELLED")
                        .set(LessonEntity::getCancelReason, "排课计划已删除")
                        .set(LessonEntity::getUpdatedAt, now));
    }

    @Override
    public List<Lesson> findSeriesLessonsForUpdate(Long seriesId, LocalDate fromDate) {
        return lessonMapper.selectList(new LambdaQueryWrapper<LessonEntity>()
                .eq(LessonEntity::getScheduleSeriesId, seriesId)
                .ge(LessonEntity::getLessonDate, fromDate).isNull(LessonEntity::getDeletedAt)
                .orderByAsc(LessonEntity::getId).last("FOR UPDATE"))
                .stream().map(this::convertToDomain).toList();
    }

    @Override
    public void updatePendingLesson(Lesson lesson, List<Long> studentIds) {
        List<LessonStudent> oldRoster = findByLessonId(lesson.getId());
        for (LessonStudent student : oldRoster) {
            if (!studentIds.contains(student.getStudentId())
                    && (!"PENDING".equals(student.getAttendanceStatus()) || Boolean.TRUE.equals(student.getShouldConsume())
                        || student.getHourTransactionId() != null)) {
                throw new com.example.training.common.exception.BusinessException(
                        com.example.training.common.exception.ErrorCode.OPERATION_NOT_ALLOWED,
                        "该课次已有签到或课时记录，不能移除相关学员");
            }
        }
        int rows = jdbcTemplate.update("""
                UPDATE lesson SET course_id=?, class_group_id=?, teacher_id=?, assistant_teacher_id_1=?,
                  assistant_teacher_id_2=?, lesson_date=?, start_time=?, end_time=?, consume_hours=?,
                  updated_by=?, updated_at=NOW(), version=version+1
                WHERE id=? AND status='PENDING' AND deleted_at IS NULL AND version=?
                """, lesson.getCourseId(), lesson.getClassGroupId(), lesson.getTeacherId(),
                lesson.getAssistantTeacherId1(), lesson.getAssistantTeacherId2(), lesson.getLessonDate(),
                lesson.getStartTime(), lesson.getEndTime(), lesson.getConsumeHours(), lesson.getUpdatedBy(),
                lesson.getId(), lesson.getVersion());
        if (rows != 1) throw new com.example.training.common.exception.BusinessException(
                com.example.training.common.exception.ErrorCode.OPERATION_NOT_ALLOWED, "课程状态已变化，请刷新后重试");
        lessonStudentMapper.delete(new LambdaQueryWrapper<LessonStudentEntity>()
                .eq(LessonStudentEntity::getLessonId, lesson.getId()).notIn(LessonStudentEntity::getStudentId, studentIds));
        jdbcTemplate.update("UPDATE lesson_student SET consume_hours=? WHERE lesson_id=? AND should_consume=0 AND hour_transaction_id IS NULL",
                lesson.getConsumeHours(), lesson.getId());
    }

    @Override
    public void softDeletePendingLessonsBySeries(Long seriesId, LocalDate fromDate) {
        lessonMapper.update(null,
                new LambdaUpdateWrapper<LessonEntity>()
                        .eq(LessonEntity::getScheduleSeriesId, seriesId)
                        .ge(LessonEntity::getLessonDate, fromDate)
                        .eq(LessonEntity::getStatus, "PENDING")
                        .isNull(LessonEntity::getDeletedAt)
                        .set(LessonEntity::getDeletedAt, LocalDateTime.now()));
    }

    @Override
    public int markAbsentForCompletedLessons(LocalDateTime threshold) {
        return lessonStudentMapper.markAbsentForCompletedLessons(threshold);
    }

    @Override
    public List<LessonStudent> findLessonStudentsByLessonId(Long lessonId) {
        return findByLessonId(lessonId);
    }
}
