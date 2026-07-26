package com.example.training.student.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.student.application.dto.BindRequest;
import com.example.training.student.application.dto.StudentCreateRequest;
import com.example.training.student.application.dto.StudentResponse;
import com.example.training.student.domain.model.ParentStudentRelation;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentApplicationService {
    
    private final StudentRepository studentRepository;
    
    @Transactional
    public StudentResponse createStudent(StudentCreateRequest request, CurrentUser currentUser) {
        studentRepository.findByStudentNo(request.getStudentNo())
                .ifPresent(s -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "学员编号已存在");
                });
        
        Student student = Student.builder()
                .studentNo(request.getStudentNo())
                .name(request.getName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phone(request.getPhone())
                .primaryTeacherId(request.getPrimaryTeacherId())
                .enrollmentDate(request.getEnrollmentDate())
                .status("ACTIVE")
                .medicalNotes(request.getMedicalNotes())
                .remark(request.getRemark())
                .createdBy(currentUser.getUserId())
                .build();
        
        Student savedStudent = studentRepository.save(student);
        return convertToResponse(savedStudent);
    }
    
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        return convertToResponse(student);
    }
    
    public List<StudentResponse> getStudentsByTeacher(Long teacherId) {
        List<Student> students = studentRepository.findByTeacherId(teacherId);
        return students.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public List<StudentResponse> getStudentsByParent(Long parentId) {
        List<Student> students = studentRepository.findByParentId(parentId);
        return students.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    @Transactional
    public void bindStudent(BindRequest request, CurrentUser currentUser) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        
        studentRepository.findRelation(currentUser.getUserId(), request.getStudentId())
                .ifPresent(relation -> {
                    if ("PENDING".equals(relation.getBindStatus())) {
                        throw new BusinessException(ErrorCode.STUDENT_BIND_PENDING, "绑定请求已存在");
                    }
                });
        
        ParentStudentRelation relation = ParentStudentRelation.builder()
                .parentId(currentUser.getUserId())
                .studentId(request.getStudentId())
                .relationType(request.getRelationType())
                .isPrimary(false)
                .bindStatus("PENDING")
                .createdBy(currentUser.getUserId())
                .build();
        
        studentRepository.saveRelation(relation);
        log.info("家长绑定学员: parentId={}, studentId={}", currentUser.getUserId(), request.getStudentId());
    }
    
    @Transactional
    public void confirmBind(Long relationId, CurrentUser currentUser) {
        ParentStudentRelation relation = studentRepository.findRelation(currentUser.getUserId(), relationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "绑定关系不存在"));
        
        relation.setBindStatus("CONFIRMED");
        relation.setBoundAt(LocalDateTime.now());
        studentRepository.saveRelation(relation);
        log.info("确认绑定: relationId={}", relationId);
    }
    
    @Transactional
    public void unbindStudent(Long studentId, CurrentUser currentUser) {
        ParentStudentRelation relation = studentRepository.findRelation(currentUser.getUserId(), studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "绑定关系不存在"));
        
        relation.setBindStatus("UNBOUND");
        studentRepository.saveRelation(relation);
        log.info("解除绑定: parentId={}, studentId={}", currentUser.getUserId(), studentId);
    }
    
    private StudentResponse convertToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gender(student.getGender())
                .birthDate(student.getBirthDate())
                .age(student.getAge())
                .phone(student.getPhone())
                .primaryTeacherId(student.getPrimaryTeacherId())
                .enrollmentDate(student.getEnrollmentDate())
                .status(student.getStatus())
                .medicalNotes(student.getMedicalNotes())
                .remark(student.getRemark())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
