package com.example.training.schedule.controller;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.security.CurrentUser;
import com.example.training.schedule.application.service.ScheduleApplicationService;
import com.example.training.schedule.application.service.ScheduleCompletionService;
import com.example.training.common.security.ParentPermissionService;
import com.example.training.common.security.TeacherPermissionService;
import com.example.training.schedule.domain.model.Lesson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleCalendarTest {
    private final ScheduleApplicationService service = mock(ScheduleApplicationService.class);
    private final ScheduleController controller = new ScheduleController(service,
            mock(ScheduleCompletionService.class), mock(ParentPermissionService.class), mock(TeacherPermissionService.class));
    private final CurrentUser teacher = CurrentUser.builder().role("TEACHER").profileId(12L).build();

    @Test void legacyCalendarIncludesSixWeeksInsteadOfTodayOnly() {
        controller.getLessonsByTeacher(null, null, null, teacher);
        LocalDate first = LocalDate.now().withDayOfMonth(1);
        LocalDate start = first.minusDays(first.getDayOfWeek().getValue() % 7);
        verify(service).getLessonsByTeacher(12L, start, start.plusDays(41));
    }
    @Test void explicitCalendarRangeIsPreserved() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        controller.getLessonsByTeacher(null, start, start.plusDays(41), teacher);
        verify(service).getLessonsByTeacher(12L, start, start.plusDays(41));
    }
    @Test void missingTeacherProfileNeverReturnsAllTeachers() {
        assertThrows(BusinessException.class, () -> controller.getLessonsByTeacher(null, null, null,
                CurrentUser.builder().role("TEACHER").build()));
        verifyNoInteractions(service);
    }
    @Test void publicTimetableDoesNotExposeStudentAttendanceOrAuditFields() throws Exception {
        LocalDate start = LocalDate.of(2026, 11, 2);
        when(service.getLessonsByTeacher(null, start, start.plusDays(6))).thenReturn(List.of(
                Lesson.builder().id(1L).teacherName("教练").studentAttendance(Map.of(99L, "PRESENT"))
                        .createdBy(88L).cancelReason("学员私人原因").build()));
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(controller.getPublicTimetable(start, start.plusDays(6)));
        assertTrue(json.contains("教练"));
        assertFalse(json.contains("studentAttendance"));
        assertFalse(json.contains("createdBy"));
        assertFalse(json.contains("cancelReason"));
    }
    @Test void publicTimetableRejectsReversedOrExcessiveRange() {
        LocalDate start = LocalDate.now();
        assertThrows(BusinessException.class, () -> controller.getPublicTimetable(start, start.minusDays(1)));
        assertThrows(BusinessException.class, () -> controller.getPublicTimetable(start, start.plusDays(7)));
        verifyNoInteractions(service);
    }
}
