package com.example.training.schedule.infrastructure.repository;

import com.example.training.schedule.infrastructure.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleRosterTest {
    @Test void persistedRosterDoesNotReintroduceRemovedStudentsFromCompletedLessons() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        LessonMapper lessons = mock(LessonMapper.class);
        LessonStudentMapper students = mock(LessonStudentMapper.class);
        ScheduleRepositoryImpl repository = new ScheduleRepositoryImpl(mock(ScheduleSeriesMapper.class), lessons, students, jdbc);
        when(jdbc.queryForList("SELECT student_id FROM schedule_series_student WHERE schedule_series_id = ?", Long.class, 301L))
                .thenReturn(List.of(102L));
        assertEquals(List.of(102L), repository.findStudentIdsByScheduleSeriesId(301L));
        verifyNoInteractions(lessons, students);
    }
}
