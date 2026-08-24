package com.example.training.course.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.training.course.domain.model.ClassGroup;
import com.example.training.course.domain.model.Course;
import com.example.training.course.domain.repository.CourseRepository;
import com.example.training.course.infrastructure.mapper.ClassGroupStudentMapper;
import com.example.training.course.infrastructure.persistence.ClassGroupEntity;
import com.example.training.course.infrastructure.persistence.ClassGroupMapper;
import com.example.training.course.infrastructure.persistence.CourseEntity;
import com.example.training.course.infrastructure.persistence.CourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {
    
    private final CourseMapper courseMapper;
    private final ClassGroupMapper classGroupMapper;
    private final ClassGroupStudentMapper classGroupStudentMapper;
    
    @Override
    public Course save(Course course) {
        CourseEntity entity = convertToEntity(course);
        if (course.getId() == null) {
            courseMapper.insert(entity);
        } else {
            courseMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<Course> findById(Long id) {
        CourseEntity entity = courseMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<Course> findByCourseCode(String courseCode) {
        CourseEntity entity = courseMapper.selectOne(
                new LambdaQueryWrapper<CourseEntity>()
                        .eq(CourseEntity::getCourseCode, courseCode)
                        .isNull(CourseEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<Course> findAllEnabled() {
        List<CourseEntity> entities = courseMapper.selectList(
                new LambdaQueryWrapper<CourseEntity>()
                        .eq(CourseEntity::getStatus, "ENABLED")
                        .isNull(CourseEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public ClassGroup saveClassGroup(ClassGroup classGroup) {
        ClassGroupEntity entity = convertToEntity(classGroup);
        if (classGroup.getId() == null) {
            classGroupMapper.insert(entity);
        } else {
            classGroupMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<ClassGroup> findClassGroupById(Long id) {
        ClassGroupEntity entity = classGroupMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<ClassGroup> findClassGroupByCode(String classCode) {
        ClassGroupEntity entity = classGroupMapper.selectOne(
                new LambdaQueryWrapper<ClassGroupEntity>()
                        .eq(ClassGroupEntity::getClassCode, classCode)
                        .isNull(ClassGroupEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<ClassGroup> findByCourseId(Long courseId) {
        List<ClassGroupEntity> entities = classGroupMapper.selectList(
                new LambdaQueryWrapper<ClassGroupEntity>()
                        .eq(ClassGroupEntity::getCourseId, courseId)
                        .isNull(ClassGroupEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<ClassGroup> findByTeacherId(Long teacherId) {
        List<ClassGroupEntity> entities = classGroupMapper.selectList(
                new LambdaQueryWrapper<ClassGroupEntity>()
                        .eq(ClassGroupEntity::getPrimaryTeacherId, teacherId)
                        .isNull(ClassGroupEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    private CourseEntity convertToEntity(Course course) {
        return CourseEntity.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .courseType(course.getCourseType())
                .defaultDurationMinutes(course.getDefaultDurationMinutes())
                .defaultConsumeHours(course.getDefaultConsumeHours())
                .description(course.getDescription())
                .status(course.getStatus())
                .createdBy(course.getCreatedBy())
                .createdAt(course.getCreatedAt())
                .updatedBy(course.getUpdatedBy())
                .updatedAt(course.getUpdatedAt())
                .deletedAt(course.getDeletedAt())
                .build();
    }
    
    private Course convertToDomain(CourseEntity entity) {
        return Course.builder()
                .id(entity.getId())
                .courseCode(entity.getCourseCode())
                .courseName(entity.getCourseName())
                .courseType(entity.getCourseType())
                .defaultDurationMinutes(entity.getDefaultDurationMinutes())
                .defaultConsumeHours(entity.getDefaultConsumeHours())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    private ClassGroupEntity convertToEntity(ClassGroup classGroup) {
        return ClassGroupEntity.builder()
                .id(classGroup.getId())
                .classCode(classGroup.getClassCode())
                .className(classGroup.getClassName())
                .courseId(classGroup.getCourseId())
                .primaryTeacherId(classGroup.getPrimaryTeacherId())
                .maxStudents(classGroup.getMaxStudents())
                .status(classGroup.getStatus())
                .startDate(classGroup.getStartDate())
                .endDate(classGroup.getEndDate())
                .remark(classGroup.getRemark())
                .createdBy(classGroup.getCreatedBy())
                .createdAt(classGroup.getCreatedAt())
                .updatedBy(classGroup.getUpdatedBy())
                .updatedAt(classGroup.getUpdatedAt())
                .deletedAt(classGroup.getDeletedAt())
                .build();
    }
    
    private ClassGroup convertToDomain(ClassGroupEntity entity) {
        return ClassGroup.builder()
                .id(entity.getId())
                .classCode(entity.getClassCode())
                .className(entity.getClassName())
                .courseId(entity.getCourseId())
                .primaryTeacherId(entity.getPrimaryTeacherId())
                .maxStudents(entity.getMaxStudents())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .remark(entity.getRemark())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    @Override
    public List<ClassGroup> findAllClassGroups() {
        List<ClassGroupEntity> entities = classGroupMapper.selectList(
                new LambdaQueryWrapper<ClassGroupEntity>()
                        .isNull(ClassGroupEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public void addStudentsToClassGroup(Long classGroupId, List<Long> studentIds, Long createdBy) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }
        classGroupStudentMapper.batchInsert(
                classGroupId,
                studentIds,
                LocalDateTime.now(),
                "ACTIVE",
                createdBy != null ? createdBy : 0L
        );
    }
    
    @Override
    public void removeStudentsFromClassGroup(Long classGroupId, List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }
        classGroupStudentMapper.batchUpdateStatus(
                classGroupId,
                studentIds,
                "LEFT",
                LocalDateTime.now()
        );
    }

    @Override
    public List<Long> findStudentIdsByClassGroupId(Long classGroupId) {
        if (classGroupId == null) {
            return java.util.Collections.emptyList();
        }
        List<com.example.training.course.infrastructure.persistence.ClassGroupStudentEntity> entities =
                classGroupStudentMapper.selectList(
                        new LambdaQueryWrapper<com.example.training.course.infrastructure.persistence.ClassGroupStudentEntity>()
                                .eq(com.example.training.course.infrastructure.persistence.ClassGroupStudentEntity::getClassGroupId, classGroupId)
                                .eq(com.example.training.course.infrastructure.persistence.ClassGroupStudentEntity::getStatus, "ACTIVE")
                );
        return entities.stream()
                .map(com.example.training.course.infrastructure.persistence.ClassGroupStudentEntity::getStudentId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}
