package com.example.training.common.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.training.common.domain.model.TeacherAttendance;
import com.example.training.common.infrastructure.persistence.TeacherAttendanceEntity;
import com.example.training.common.infrastructure.persistence.TeacherAttendanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TeacherAttendanceRepositoryImpl implements TeacherAttendanceRepository {

    private final TeacherAttendanceMapper teacherAttendanceMapper;

    @Override
    public TeacherAttendance save(TeacherAttendance attendance) {
        TeacherAttendanceEntity entity = convertToEntity(attendance);
        if (attendance.getId() == null) {
            teacherAttendanceMapper.insert(entity);
        } else {
            teacherAttendanceMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }

    @Override
    public Optional<TeacherAttendance> findById(Long id) {
        TeacherAttendanceEntity entity = teacherAttendanceMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }

    @Override
    public List<TeacherAttendance> findByTeacherIdAndDateRange(Long teacherId, LocalDate startDate, LocalDate endDate) {
        List<TeacherAttendanceEntity> entities = teacherAttendanceMapper.selectList(
                new LambdaQueryWrapper<TeacherAttendanceEntity>()
                        .eq(TeacherAttendanceEntity::getTeacherId, teacherId)
                        .ge(TeacherAttendanceEntity::getAttendanceDate, startDate)
                        .le(TeacherAttendanceEntity::getAttendanceDate, endDate)
                        .isNull(TeacherAttendanceEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }

    @Override
    public List<TeacherAttendance> findAllByDateRange(LocalDate startDate, LocalDate endDate) {
        List<TeacherAttendanceEntity> entities = teacherAttendanceMapper.selectList(
                new LambdaQueryWrapper<TeacherAttendanceEntity>()
                        .ge(TeacherAttendanceEntity::getAttendanceDate, startDate)
                        .le(TeacherAttendanceEntity::getAttendanceDate, endDate)
                        .isNull(TeacherAttendanceEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }

    private TeacherAttendanceEntity convertToEntity(TeacherAttendance domain) {
        TeacherAttendanceEntity entity = new TeacherAttendanceEntity();
        entity.setId(domain.getId());
        entity.setTeacherId(domain.getTeacherId());
        entity.setLessonId(domain.getLessonId());
        entity.setAttendanceDate(domain.getAttendanceDate());
        entity.setAttendanceStatus(domain.getAttendanceStatus());
        entity.setCheckInTime(domain.getCheckInTime());
        entity.setCheckOutTime(domain.getCheckOutTime());
        entity.setLeaveReason(domain.getLeaveReason());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    private TeacherAttendance convertToDomain(TeacherAttendanceEntity entity) {
        return TeacherAttendance.builder()
                .id(entity.getId())
                .teacherId(entity.getTeacherId())
                .lessonId(entity.getLessonId())
                .attendanceDate(entity.getAttendanceDate())
                .attendanceStatus(entity.getAttendanceStatus())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .leaveReason(entity.getLeaveReason())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
