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
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StudentRepositoryImpl implements StudentRepository {

    private final StudentMapper studentMapper;
    private final ParentStudentRelationMapper relationMapper;
    private final SqlSessionFactory sqlSessionFactory;
    
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
    public List<Student> findAll() {
        List<StudentEntity> entities = studentMapper.selectList(
                new LambdaQueryWrapper<StudentEntity>()
                        .isNull(StudentEntity::getDeletedAt)
                        .orderByDesc(StudentEntity::getId)
        );
        return entities.stream().map(this::convertToDomain).collect(Collectors.toList());
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
    public boolean existsByTeacherAndStudentId(Long teacherId, Long studentId) {
        if (teacherId == null || studentId == null) return false;
        Long count = studentMapper.selectCount(
                new LambdaQueryWrapper<StudentEntity>()
                        .eq(StudentEntity::getId, studentId)
                        .eq(StudentEntity::getPrimaryTeacherId, teacherId)
                        .isNull(StudentEntity::getDeletedAt)
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<Student> findByBindMatch(String studentNo, String name, String parentPhone) {
        LambdaQueryWrapper<StudentEntity> q = new LambdaQueryWrapper<StudentEntity>()
                .isNull(StudentEntity::getDeletedAt);
        if (studentNo != null && !studentNo.isBlank()) {
            q.eq(StudentEntity::getStudentNo, studentNo.trim());
        } else {
            return Optional.empty();
        }
        if (name != null && !name.isBlank()) {
            q.eq(StudentEntity::getName, name.trim());
        }
        if (parentPhone != null && !parentPhone.isBlank()) {
            String trim = parentPhone.trim();
            q.and(w -> w.eq(StudentEntity::getParentPhone, trim)
                    .or()
                    .likeRight(StudentEntity::getPhone, trim));
        }
        StudentEntity entity = studentMapper.selectOne(q);
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }
    
    @Override
    public List<Student> findByParentId(Long profileOrUserId) {
        if (profileOrUserId == null) return Collections.emptyList();
        List<Long> studentIds = new ArrayList<>();
        try {
            DataSource ds = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT DISTINCT psr.student_id FROM parent_student_relation psr " +
                                 "WHERE psr.deleted_at IS NULL AND psr.bind_status = 'CONFIRMED' AND (" +
                                 "  psr.parent_id = ? " +
                                 "  OR psr.parent_id IN (" +
                                 "     SELECT pp.id FROM parent_profile pp " +
                                 "     WHERE pp.deleted_at IS NULL AND pp.user_id = ?)" +
                                 "  OR EXISTS (" +
                                 "     SELECT 1 FROM parent_profile pp2 " +
                                 "     WHERE pp2.deleted_at IS NULL AND pp2.id = ? " +
                                 "       AND psr.parent_id = pp2.user_id)" +
                                 ")")) {
                ps.setLong(1, profileOrUserId);
                ps.setLong(2, profileOrUserId);
                ps.setLong(3, profileOrUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) studentIds.add(rs.getLong(1));
                }
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
        if (studentIds.isEmpty()) return Collections.emptyList();
        return studentIds.stream()
                .map(studentMapper::selectById)
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
    
    private List<Long> resolveParentIdCandidates(Long profileOrUserId) {
        if (profileOrUserId == null) return Collections.emptyList();
        List<Long> result = new ArrayList<>();
        result.add(profileOrUserId);
        try {
            DataSource ds = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT pp.user_id FROM parent_profile pp " +
                                 "WHERE pp.deleted_at IS NULL AND pp.id = ? " +
                                 "UNION " +
                                 "SELECT pp2.id FROM parent_profile pp2 " +
                                 "WHERE pp2.deleted_at IS NULL AND pp2.user_id = ?")) {
                ps.setLong(1, profileOrUserId);
                ps.setLong(2, profileOrUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long v = rs.getLong(1);
                        if (!result.contains(v)) result.add(v);
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return result;
    }

    @Override
    public Optional<ParentStudentRelation> findRelation(Long parentId, Long studentId) {
        List<Long> candidates = resolveParentIdCandidates(parentId);
        ParentStudentRelationEntity entity = relationMapper.selectOne(
                new LambdaQueryWrapper<ParentStudentRelationEntity>()
                        .in(ParentStudentRelationEntity::getParentId, candidates)
                        .eq(ParentStudentRelationEntity::getStudentId, studentId)
        );
        return Optional.ofNullable(entity).map(this::convertToDomain);
    }

    @Override
    public List<ParentStudentRelation> findRelationsByParentId(Long parentId) {
        List<Long> candidates = resolveParentIdCandidates(parentId);
        if (candidates.isEmpty()) return Collections.emptyList();
        List<ParentStudentRelationEntity> entities = relationMapper.selectList(
                new LambdaQueryWrapper<ParentStudentRelationEntity>()
                        .in(ParentStudentRelationEntity::getParentId, candidates)
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
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .parentRelation(student.getParentRelation())
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
                .parentName(entity.getParentName())
                .parentPhone(entity.getParentPhone())
                .parentRelation(entity.getParentRelation())
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
