package com.example.training.common.infrastructure.repository;

import com.example.training.common.domain.model.TeacherAttendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TeacherAttendanceRepository {

    TeacherAttendance save(TeacherAttendance attendance);

    Optional<TeacherAttendance> findById(Long id);

    List<TeacherAttendance> findByTeacherIdAndDateRange(Long teacherId, LocalDate startDate, LocalDate endDate);

    List<TeacherAttendance> findAllByDateRange(LocalDate startDate, LocalDate endDate);
}
