package com.example.training.schedule.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        ScheduleSeriesEntity entity = scheduleSeriesMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<ScheduleSeries> findByTeacherId(Long teacherId) {
        List<ScheduleSeriesEntity> entities = scheduleSeriesMapper.selectList(
                new LambdaQueryWrapper<ScheduleSeriesEntity>()
                        .eq(ScheduleSeriesEntity::getTeacherId, teacherId)
                        .isNull(ScheduleSeriesEntity::getDeletedAt)
        );
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
        } else {
            lessonMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<Lesson> findLessonById(Long id) {
        LessonEntity entity = lessonMapper.selectById(id);
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
        List<LessonEntity> entities = lessonMapper.selectList(
                new LambdaQueryWrapper<LessonEntity>()
                        .eq(LessonEntity::getTeacherId, teacherId)
                        .eq(LessonEntity::getLessonDate, date)
                        .isNull(LessonEntity::getDeletedAt)
        );
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
        return List.of();
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
        scheduleSeriesMapper.deleteById(id);
        lessonMapper.delete(
                new LambdaQueryWrapper<LessonEntity>()
                        .eq(LessonEntity::getScheduleSeriesId, id)
        );
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
