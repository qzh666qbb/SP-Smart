package com.example.training.student.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.training.student.domain.model.ParentStudentRelation;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
import com.example.training.student.infrastructure.persistence.ParentStudentRelationEntity;
import com.example.training.student.infrastructure.persistence.ParentStudentRelationMapper;
import com.example.training.student.infrastructure.persistence.StudentEntity;
import com.example.training.student.infrastructure.persistence.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StudentRepositoryImpl implements StudentRepository {
    
    private final StudentMapper studentMapper;
    private final ParentStudentRelationMapper relationMapper;
    
    @Override
    public Student save(Student student) {
        StudentEntity entity = convertToEntity(student);
        if (student.getId() == null) {
            studentMapper.insert(entity);
        } else {
            studentMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<Student> findById(Long id) {
        StudentEntity entity = studentMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public Optional<Student> findByStudentNo(String studentNo) {
        StudentEntity entity = studentMapper.selectOne(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getStudentNo, studentNo)
                        .isNull(StudentEntity::getDeletedAt)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<Student> findByTeacherId(Long teacherId) {
        List<StudentEntity> entities = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getPrimaryTeacherId, teacherId)
                        .isNull(StudentEntity::getDeletedAt)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Student> findByParentId(Long parentId) {
        List<ParentStudentRelationEntity> relations = relationMapper.selectList(
                new LambdaQueryWrapper<ParentStudentRelationEntity>()
                        .eq(ParentStudentRelationEntity::getParentId, parentId)
                        .eq(ParentStudentRelationEntity::getBindStatus, "CONFIRMED")
        );
        
        return relations.stream()
                .map(r -> studentMapper.selectById(r.getStudentId()))
                .filter(e -> e != null && e.getDeletedAt() == null)
                .map(this::convertToDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public ParentStudentRelation saveRelation(ParentStudentRelation relation) {
        ParentStudentRelationEntity entity = convertToEntity(relation);
        if (relation.getId() == null) {
            relationMapper.insert(entity);
        } else {
            relationMapper.updateById(entity);
        }
        return convertToDomain(entity);
    }
    
    @Override
    public Optional<ParentStudentRelation> findRelation(Long parentId, Long studentId) {
        ParentStudentRelationEntity entity = relationMapper.selectOne(
                new LambdaQueryWrapper<ParentStudentRelationEntity>()
                        .eq(ParentStudentRelationEntity::getParentId, parentId)
                        .eq(ParentStudentRelationEntity::getStudentId, studentId)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<ParentStudentRelation> findRelationsByParentId(Long parentId) {
        List<ParentStudentRelationEntity> entities = relationMapper.selectList(
                new LambdaQueryWrapper<ParentStudentRelationEntity>()
                        .eq(ParentStudentRelationEntity::getParentId, parentId)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
    }
    
    @Override
    public void deleteRelation(Long id) {
        relationMapper.deleteById(id);
    }
    
    private StudentEntity convertToEntity(Student student) {
        return StudentEntity.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gender(student.getGender())
                .birthDate(student.getBirthDate())
                .phone(student.getPhone())
                .primaryTeacherId(student.getPrimaryTeacherId())
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .medicalNotes(student.getMedicalNotes())
                .remark(student.getRemark())
                .createdBy(student.getCreatedBy())
                .createdAt(student.getCreatedAt())
                .updatedBy(student.getUpdatedBy())
                .updatedAt(student.getUpdatedAt())
                .deletedAt(student.getDeletedAt())
                .build();
    }
    
    private Student convertToDomain(StudentEntity entity) {
        return Student.builder()
                .id(entity.getId())
                .studentNo(entity.getStudentNo())
                .name(entity.getName())
                .gender(entity.getGender())
                .birthDate(entity.getBirthDate())
                .phone(entity.getPhone())
                .primaryTeacherId(entity.getPrimaryTeacherId())
                .enrollmentDate(entity.getEnrollmentDate())
                .status(entity.getStatus())
                .medicalNotes(entity.getMedicalNotes())
                .remark(entity.getRemark())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
    
    private ParentStudentRelationEntity convertToEntity(ParentStudentRelation relation) {
        return ParentStudentRelationEntity.builder()
                .id(relation.getId())
                .parentId(relation.getParentId())
                .studentId(relation.getStudentId())
                .relationType(relation.getRelationType())
                .isPrimary(relation.getIsPrimary())
                .bindStatus(relation.getBindStatus())
                .boundAt(relation.getBoundAt())
                .createdBy(relation.getCreatedBy())
                .createdAt(relation.getCreatedAt())
                .updatedAt(relation.getUpdatedAt())
                .build();
    }
    
    private ParentStudentRelation convertToDomain(ParentStudentRelationEntity entity) {
        return ParentStudentRelation.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .studentId(entity.getStudentId())
                .relationType(entity.getRelationType())
                .isPrimary(entity.getIsPrimary())
                .bindStatus(entity.getBindStatus())
                .boundAt(entity.getBoundAt())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
