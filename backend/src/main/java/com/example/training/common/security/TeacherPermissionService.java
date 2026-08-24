package com.example.training.common.security;

import com.example.training.student.domain.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherPermissionService {

    private final StudentRepository studentRepository;

    public boolean canAccessStudent(CurrentUser user, Long studentId) {
        if (user == null || studentId == null) {
            return false;
        }
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        if ("TEACHER".equals(user.getRole())) {
            return studentRepository.existsByTeacherAndStudentId(user.getProfileId(), studentId);
        }
        return false;
    }
}
