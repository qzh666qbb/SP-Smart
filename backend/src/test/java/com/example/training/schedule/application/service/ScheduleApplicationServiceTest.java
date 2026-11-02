package com.example.training.schedule.application.service;

import com.example.training.common.exception.BusinessException;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.course.domain.model.Course;
import com.example.training.common.security.CurrentUser;
import com.example.training.course.domain.repository.CourseRepository;
import com.example.training.hour.application.service.HourApplicationService;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.schedule.application.dto.ScheduleCreateRequest;
import com.example.training.schedule.application.dto.AttendanceRequest;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.model.ScheduleSeries;
import com.example.training.schedule.domain.repository.ScheduleRepository;
import com.example.training.student.domain.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleApplicationServiceTest {

    @Mock ScheduleRepository scheduleRepository;
    @Mock CourseRepository courseRepository;
    @Mock StudentRepository studentRepository;
    @Mock HourApplicationService hourApplicationService;
    @Mock UserRepository userRepository;

    private ScheduleApplicationService service;
    private CurrentUser teacher;

    @BeforeEach
    void setUp() {
        service = new ScheduleApplicationService(scheduleRepository, courseRepository, studentRepository, hourApplicationService, userRepository);
        teacher = CurrentUser.builder().userId(6L).role("TEACHER").profileId(21L).build();
        org.mockito.Mockito.lenient().when(courseRepository.findById(any()))
                .thenReturn(Optional.of(Course.builder().id(1L).courseName("测试课程").status("ENABLED").build()));
        org.mockito.Mockito.lenient().when(userRepository.findTeacherProfileById(any()))
                .thenReturn(Optional.of(TeacherProfile.builder().id(21L).name("测试教师").employmentStatus("ACTIVE").build()));
        org.mockito.Mockito.lenient().when(studentRepository.findById(any()))
                .thenReturn(Optional.of(com.example.training.student.domain.model.Student.builder().id(101L).build()));
    }

    @Test
    void createsLessonForAuthenticatedTeacherAndAttachesExplicitStudents() {
        ScheduleCreateRequest request = request(List.of(101L, 102L));
        request.setTeacherId(999L);

        when(studentRepository.existsByTeacherAndStudentId(21L, 101L)).thenReturn(true);
        when(studentRepository.existsByTeacherAndStudentId(21L, 102L)).thenReturn(true);
        when(scheduleRepository.saveScheduleSeries(any())).thenAnswer(invocation -> {
            ScheduleSeries series = invocation.getArgument(0);
            series.setId(301L);
            return series;
        });
        when(scheduleRepository.saveLesson(any())).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(401L);
            return lesson;
        });
        when(scheduleRepository.findLessonStudent(any(), any())).thenReturn(Optional.empty());

        service.createScheduleSeries(request, teacher);

        ArgumentCaptor<ScheduleSeries> seriesCaptor = ArgumentCaptor.forClass(ScheduleSeries.class);
        verify(scheduleRepository).saveScheduleSeries(seriesCaptor.capture());
        assertEquals(21L, seriesCaptor.getValue().getTeacherId());

        ArgumentCaptor<LessonStudent> relationCaptor = ArgumentCaptor.forClass(LessonStudent.class);
        verify(scheduleRepository, org.mockito.Mockito.times(2)).saveLessonStudent(relationCaptor.capture());
        assertEquals(List.of(101L, 102L), relationCaptor.getAllValues().stream().map(LessonStudent::getStudentId).toList());
    }

    @Test
    void rejectsStudentOutsideAuthenticatedTeachersScope() {
        ScheduleCreateRequest request = request(List.of(888L));
        when(studentRepository.existsByTeacherAndStudentId(21L, 888L)).thenReturn(false);
        when(scheduleRepository.saveScheduleSeries(any())).thenAnswer(invocation -> {
            ScheduleSeries series = invocation.getArgument(0);
            series.setId(301L);
            return series;
        });

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.createScheduleSeries(request, teacher));

        assertEquals("只能为本人负责的学员排课", error.getMessage());
        verify(scheduleRepository, never()).saveLesson(any());
    }

    @Test
    void rejectsStartingAnotherTeachersLesson() {
        Lesson lesson = Lesson.builder().id(401L).teacherId(999L).status("PENDING").build();
        when(scheduleRepository.findLessonById(401L)).thenReturn(Optional.of(lesson));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.startLesson(401L, teacher));

        assertEquals("只能操作本人负责的课程", error.getMessage());
        verify(scheduleRepository, never()).saveLesson(any());
    }

    @Test
    void rejectsAttendanceForStudentOutsideLessonRoster() {
        Lesson lesson = Lesson.builder().id(401L).teacherId(21L).status("IN_PROGRESS").build();
        AttendanceRequest request = AttendanceRequest.builder()
                .lessonId(401L)
                .studentId(888L)
                .attendanceStatus("PRESENT")
                .build();
        when(scheduleRepository.findLessonById(401L)).thenReturn(Optional.of(lesson));
        when(scheduleRepository.findLessonStudent(401L, 888L)).thenReturn(Optional.empty());

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.recordAttendance(request, teacher));

        assertEquals("学员不在本次课程名单中", error.getMessage());
        verify(scheduleRepository, never()).saveLessonStudent(any());
    }

    @Test
    void rejectsTeacherAttendanceBeforeLessonStarts() {
        Lesson lesson = Lesson.builder().id(401L).teacherId(21L).status("PENDING").build();
        AttendanceRequest request = AttendanceRequest.builder()
                .lessonId(401L).studentId(101L).attendanceStatus("PRESENT").build();
        when(scheduleRepository.findLessonById(401L)).thenReturn(Optional.of(lesson));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.recordAttendance(request, teacher));

        assertEquals("课程开始后才能记录考勤", error.getMessage());
        verify(scheduleRepository, never()).saveLessonStudent(any());
    }

    @Test
    void rejectsAdminAttendanceForPendingLesson() {
        CurrentUser admin = CurrentUser.builder().userId(1L).role("ADMIN").build();
        Lesson lesson = Lesson.builder().id(402L).status("PENDING").build();
        AttendanceRequest request = AttendanceRequest.builder()
                .lessonId(402L).studentId(101L).attendanceStatus("PRESENT").build();
        when(scheduleRepository.findLessonById(402L)).thenReturn(Optional.of(lesson));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.recordAttendance(request, admin));

        assertEquals("只能为进行中或已完成的课程记录考勤", error.getMessage());
        verify(scheduleRepository, never()).saveLessonStudent(any());
    }

    @Test
    void adminAddsCompletedAttendanceAndConsumesHours() {
        CurrentUser admin = CurrentUser.builder().userId(1L).role("ADMIN").build();
        Lesson lesson = Lesson.builder().id(403L).lessonNo("LSN403").status("COMPLETED")
                .consumeHours(BigDecimal.ONE).build();
        LessonStudent student = LessonStudent.builder().id(40L).lessonId(403L).studentId(101L)
                .attendanceStatus("ABSENT").shouldConsume(false).consumeHours(BigDecimal.ONE).build();
        AttendanceRequest request = AttendanceRequest.builder()
                .lessonId(403L).studentId(101L).attendanceStatus("LATE").build();
        when(scheduleRepository.findLessonById(403L)).thenReturn(Optional.of(lesson));
        when(scheduleRepository.findLessonStudent(403L, 101L)).thenReturn(Optional.of(student));
        when(scheduleRepository.saveLessonStudent(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hourApplicationService.consume(any(), any(), any(), any(), any(), any()))
                .thenReturn(HourTransaction.builder().id(700L).build());

        service.recordAttendance(request, admin);

        assertEquals("LATE", student.getAttendanceStatus());
        assertEquals(true, student.getShouldConsume());
        assertEquals(700L, student.getHourTransactionId());
    }

    @Test
    void adminRemovesCompletedAttendanceAndRefundsHours() {
        CurrentUser admin = CurrentUser.builder().userId(1L).role("ADMIN").build();
        Lesson lesson = Lesson.builder().id(404L).lessonNo("LSN404").status("COMPLETED").build();
        LessonStudent student = LessonStudent.builder().id(41L).lessonId(404L).studentId(101L)
                .attendanceStatus("PRESENT").shouldConsume(true).hourTransactionId(701L).build();
        AttendanceRequest request = AttendanceRequest.builder()
                .lessonId(404L).studentId(101L).attendanceStatus("ABSENT").build();
        when(scheduleRepository.findLessonById(404L)).thenReturn(Optional.of(lesson));
        when(scheduleRepository.findLessonStudent(404L, 101L)).thenReturn(Optional.of(student));
        when(scheduleRepository.saveLessonStudent(any())).thenAnswer(inv -> inv.getArgument(0));

        service.recordAttendance(request, admin);

        verify(hourApplicationService).refundByTransaction(701L, "管理员修正考勤退回 - LSN404", admin);
        assertEquals(false, student.getShouldConsume());
        assertEquals(null, student.getHourTransactionId());
    }

    @Test
    void returnsChildAttendanceStateWithLesson() {
        Lesson lesson = Lesson.builder().id(401L).courseId(1L).teacherId(21L).build();
        LessonStudent attendance = LessonStudent.builder()
                .lessonId(401L).studentId(101L).attendanceStatus("PRESENT").build();
        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);
        when(scheduleRepository.findByStudentIdAndDateRange(101L, start, end)).thenReturn(List.of(lesson));
        when(scheduleRepository.findLessonStudent(401L, 101L)).thenReturn(Optional.of(attendance));

        List<Lesson> result = service.getChildSchedules(101L, start, end, teacher);

        assertEquals("PRESENT", result.get(0).getStudentAttendance().get(101L));
    }

    @Test
    void queriesTeacherLessonsByDateRange() {
        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);
        List<Lesson> expected = List.of(Lesson.builder().id(401L).teacherId(21L).build());
        when(scheduleRepository.findByTeacherIdAndDateRange(21L, startDate, endDate)).thenReturn(expected);

        assertEquals(expected, service.getLessonsByTeacher(21L, startDate, endDate));
        verify(scheduleRepository).findByTeacherIdAndDateRange(21L, startDate, endDate);
    }

    @Test
    void rejectsReadingAnotherTeachersScheduleSeries() {
        ScheduleSeries series = ScheduleSeries.builder().id(301L).teacherId(999L).build();
        when(scheduleRepository.findScheduleSeriesById(301L)).thenReturn(Optional.of(series));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.getSchedule(301L, teacher));

        assertEquals("只能查看本人负责的排课", error.getMessage());
    }

    @Test
    void rejectsOverlappingTeacherSchedule() {
        ScheduleCreateRequest request = request(List.of(101L));
        request.setTeacherId(21L);
        Lesson occupied = Lesson.builder()
                .id(501L).teacherId(21L).lessonDate(request.getStartDate())
                .startTime(LocalTime.of(9, 30)).endTime(LocalTime.of(10, 30)).status("PENDING")
                .build();
        when(scheduleRepository.findByTeacherIdAndDateRange(21L, request.getStartDate(), request.getStartDate()))
                .thenReturn(List.of(occupied));
        CurrentUser admin = CurrentUser.builder().userId(1L).role("ADMIN").build();

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.createScheduleSeries(request, admin));

        org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("时间冲突"));
        verify(scheduleRepository, never()).saveScheduleSeries(any());
    }

    @Test
    void rejectsRatingStudentOutsideLessonRoster() {
        Lesson completed = Lesson.builder().id(401L).status("COMPLETED").build();
        when(scheduleRepository.findLessonById(401L)).thenReturn(Optional.of(completed));
        when(scheduleRepository.findLessonStudent(401L, 888L)).thenReturn(Optional.empty());

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.submitRating(401L, 888L, BigDecimal.valueOf(5), null));

        assertEquals("学员不在本次课程名单中", error.getMessage());
        verify(scheduleRepository, never()).saveLessonStudent(any());
    }

    private ScheduleCreateRequest request(List<Long> studentIds) {
        return ScheduleCreateRequest.builder()
                .courseId(1L)
                .studentIds(studentIds)
                .recurrenceType("NONE")
                .startDate(LocalDate.of(2026, 9, 18))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .consumeHours(BigDecimal.ONE)
                .build();
    }
}
