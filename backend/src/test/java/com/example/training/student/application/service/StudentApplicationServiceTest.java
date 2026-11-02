package com.example.training.student.application.service;

import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.domain.model.StudentHourAccount;
import com.example.training.hour.domain.repository.HourRepository;
import com.example.training.student.application.dto.StudentCreateRequest;
import com.example.training.student.domain.model.Student;
import com.example.training.student.domain.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentApplicationServiceTest {

    @Mock StudentRepository studentRepository;
    @Mock UserRepository userRepository;
    @Mock HourRepository hourRepository;

    private StudentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new StudentApplicationService(studentRepository, userRepository, hourRepository);
    }

    @Test
    void createStudentUsesSmallestAvailableThreeDigitNumber() {
        when(studentRepository.findAll()).thenReturn(List.of(
                Student.builder().studentNo("001").build(),
                Student.builder().studentNo("003").build(),
                Student.builder().studentNo("S202610260001").build()));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(10L);
            return student;
        });
        when(hourRepository.findAccountByStudentId(10L))
                .thenReturn(Optional.of(StudentHourAccount.builder().studentId(10L).build()));

        StudentCreateRequest request = StudentCreateRequest.builder()
                .studentNo("999")
                .name("测试学员")
                .gender("UNKNOWN")
                .build();
        CurrentUser admin = CurrentUser.builder().userId(1L).role("ADMIN").build();

        service.createStudent(request, admin);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertEquals("002", captor.getValue().getStudentNo());
    }
}
