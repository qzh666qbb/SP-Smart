package com.example.training.common.security;
import com.example.training.student.domain.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class TeacherPermissionServiceTest {
    @Test void teachingAssignmentAllowsReadButNotStudentProfileChanges() {
        var repository = mock(StudentRepository.class);
        var service = new TeacherPermissionService(repository);
        var teacher = CurrentUser.builder().role("TEACHER").profileId(2L).build();
        when(repository.canTeacherAccessStudent(2L, 5L)).thenReturn(true);
        assertTrue(service.canAccessStudent(teacher, 5L));
        assertFalse(service.canManageStudent(teacher, 5L));
        assertFalse(service.canAccessStudent(teacher, 6L));
        assertFalse(service.canAccessStudent(null, 5L));
    }
}
