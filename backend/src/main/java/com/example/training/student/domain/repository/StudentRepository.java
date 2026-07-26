package com.example.training.student.domain.repository;

import com.example.training.student.domain.model.ParentStudentRelation;
import com.example.training.student.domain.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    
    Student save(Student student);
    
    Optional<Student> findById(Long id);
    
    Optional<Student> findByStudentNo(String studentNo);
    
    List<Student> findByTeacherId(Long teacherId);
    
    List<Student> findByParentId(Long parentId);
    
    ParentStudentRelation saveRelation(ParentStudentRelation relation);
    
    Optional<ParentStudentRelation> findRelation(Long parentId, Long studentId);
    
    List<ParentStudentRelation> findRelationsByParentId(Long parentId);
    
    void deleteRelation(Long id);
}
