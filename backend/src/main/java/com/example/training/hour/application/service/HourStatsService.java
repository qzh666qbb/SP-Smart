package com.example.training.hour.application.service;

import com.example.training.hour.application.dto.HourConsumeStatsResponse;
import com.example.training.hour.application.dto.HourRankResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HourStatsService {

    private final JdbcTemplate jdbcTemplate;

    public HourConsumeStatsResponse getConsumeStats(LocalDate startDate, LocalDate endDate, String courseType) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        Object[] params = {start, end};

        String baseWhere = "WHERE ht.created_at BETWEEN ? AND ? AND ht.transaction_type = 'CONSUME'";

        String totalSql = "SELECT COALESCE(SUM(ABS(ht.change_amount)), 0) as total_hours, COUNT(*) as total_count " +
                "FROM hour_transaction ht " + baseWhere;
        Map<String, Object> total = jdbcTemplate.queryForMap(totalSql, params);

        HourConsumeStatsResponse response = new HourConsumeStatsResponse();
        response.setTotalHours((BigDecimal) total.get("total_hours"));
        response.setTotalCount(((Number) total.get("total_count")).intValue());

        Map<String, BigDecimal> byCourseType = new HashMap<>();
        if ("ALL".equals(courseType) || courseType == null) {
            String typeSql = "SELECT c.course_type, COALESCE(SUM(ABS(ht.change_amount)), 0) as hours " +
                    "FROM hour_transaction ht " +
                    "JOIN lesson l ON ht.lesson_id = l.id " +
                    "JOIN course c ON l.course_id = c.id " +
                    baseWhere + " " +
                    "GROUP BY c.course_type";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(typeSql, params);
            for (Map<String, Object> row : rows) {
                byCourseType.put((String) row.get("course_type"), (BigDecimal) row.get("hours"));
            }
        } else {
            response.setTotalHours(BigDecimal.ZERO);
        }

        response.setByCourseType(byCourseType);
        return response;
    }

    public List<HourRankResponse> getTeacherConsumeRank(LocalDate startDate, LocalDate endDate, int topN) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        Object[] params = {start, end, topN};

        String sql = """
            SELECT u.id as teacher_id, u.username as teacher_name, 
                   COALESCE(SUM(ABS(ht.change_amount)), 0) as total_hours,
                   COUNT(*) as consume_count
            FROM hour_transaction ht
            JOIN sys_user u ON ht.operator_id = u.id
            WHERE ht.transaction_type = 'CONSUME'
              AND ht.operator_role = 'TEACHER'
              AND ht.created_at BETWEEN ? AND ?
            GROUP BY u.id, u.username
            ORDER BY total_hours DESC
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            HourRankResponse r = new HourRankResponse();
            r.setId(rs.getLong("teacher_id"));
            r.setName(rs.getString("teacher_name"));
            r.setValue(rs.getBigDecimal("total_hours"));
            r.setCount(rs.getInt("consume_count"));
            return r;
        });
    }

    public List<HourRankResponse> getStudentConsumeRank(LocalDate startDate, LocalDate endDate, int topN) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        Object[] params = {start, end, topN};

        String sql = """
            SELECT s.id as student_id, s.name as student_name,
                   COALESCE(SUM(ABS(ht.change_amount)), 0) as total_hours,
                   COUNT(*) as consume_count
            FROM hour_transaction ht
            JOIN student s ON ht.student_id = s.id
            WHERE ht.transaction_type = 'CONSUME'
              AND ht.created_at BETWEEN ? AND ?
            GROUP BY s.id, s.name
            ORDER BY total_hours DESC
            LIMIT ?
            """;
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            HourRankResponse r = new HourRankResponse();
            r.setId(rs.getLong("student_id"));
            r.setName(rs.getString("student_name"));
            r.setValue(rs.getBigDecimal("total_hours"));
            r.setCount(rs.getInt("consume_count"));
            return r;
        });
    }

    public Map<String, Object> getAttendanceStats(LocalDate startDate, LocalDate endDate, Long teacherId) {
        Map<String, Object> result = new HashMap<>();

        String studentSql;
        Object[] studentParams;
        if (teacherId != null) {
            studentSql = """
                SELECT ls.attendance_status, COUNT(*) as cnt
                FROM lesson_student ls
                JOIN lesson l ON ls.lesson_id = l.id
                WHERE l.lesson_date BETWEEN ? AND ? AND l.teacher_id = ?
                GROUP BY ls.attendance_status
                """;
            studentParams = new Object[]{startDate, endDate, teacherId};
        } else {
            studentSql = """
                SELECT ls.attendance_status, COUNT(*) as cnt
                FROM lesson_student ls
                JOIN lesson l ON ls.lesson_id = l.id
                WHERE l.lesson_date BETWEEN ? AND ?
                GROUP BY ls.attendance_status
                """;
            studentParams = new Object[]{startDate, endDate};
        }

        Map<String, Integer> studentAttendance = new HashMap<>();
        List<Map<String, Object>> studentRows = jdbcTemplate.queryForList(studentSql, studentParams);
        for (Map<String, Object> row : studentRows) {
            studentAttendance.put((String) row.get("attendance_status"), ((Number) row.get("cnt")).intValue());
        }
        result.put("studentAttendance", studentAttendance);

        if (teacherId == null) {
            String teacherSql = """
                SELECT ta.attendance_status, COUNT(*) as cnt
                FROM teacher_attendance ta
                WHERE ta.attendance_date BETWEEN ? AND ?
                GROUP BY ta.attendance_status
                """;
            Map<String, Integer> teacherAttendance = new HashMap<>();
            List<Map<String, Object>> teacherRows = jdbcTemplate.queryForList(teacherSql, startDate, endDate);
            for (Map<String, Object> row : teacherRows) {
                teacherAttendance.put((String) row.get("attendance_status"), ((Number) row.get("cnt")).intValue());
            }
            result.put("teacherAttendance", teacherAttendance);
        }

        return result;
    }
}
