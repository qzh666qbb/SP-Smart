package com.example.training.course.domain.repository;

import com.example.training.course.domain.model.ClassGroup;
import com.example.training.course.domain.model.Course;

import java.util.List;
import java.util.Optional;

public interface CourseRepository {
    
    Course save(Course course);
    
    Optional<Course> findById(Long id);
    
    Optional<Course> findByCourseCode(String courseCode);
    
    List<Course> findAllEnabled();
    
    ClassGroup saveClassGroup(ClassGroup classGroup);
    
    Optional<ClassGroup> findClassGroupById(Long id);
    
    Optional<ClassGroup> findClassGroupByCode(String classCode);
    
    List<ClassGroup> findByCourseId(Long courseId);
    
    List<ClassGroup> findByTeacherId(Long teacherId);
    
    List<ClassGroup> findAllClassGroups();
    
    void addStudentsToClassGroup(Long classGroupId, List<Long> studentIds, Long createdBy);
    
    void removeStudentsFromClassGroup(Long classGroupId, List<Long> studentIds);

    List<Long> findStudentIdsByClassGroupId(Long classGroupId);
}
