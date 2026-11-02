package com.example.training.student.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<StudentEntity> {
    String ACCESSIBLE = """
        FROM student st
        WHERE st.deleted_at IS NULL AND (
            st.primary_teacher_id = #{teacherId} OR EXISTS (
                SELECT 1 FROM schedule_series_student ss
                JOIN schedule_series s ON s.id = ss.schedule_series_id
                WHERE ss.student_id = st.id AND s.deleted_at IS NULL AND s.status = 'ACTIVE'
                  AND (s.end_date IS NULL OR s.end_date >= CURRENT_DATE())
                  AND (s.recurrence_type <> 'NONE' OR s.start_date >= CURRENT_DATE())
                  AND (s.teacher_id = #{teacherId} OR s.assistant_teacher_id_1 = #{teacherId}
                       OR s.assistant_teacher_id_2 = #{teacherId})
            )
        )
        """;

    @org.apache.ibatis.annotations.Select("SELECT st.* " + ACCESSIBLE + " ORDER BY st.id DESC")
    java.util.List<StudentEntity> selectAccessibleStudents(@org.apache.ibatis.annotations.Param("teacherId") Long teacherId);

    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) " + ACCESSIBLE + " AND st.id = #{studentId}")
    long countAccessibleStudent(@org.apache.ibatis.annotations.Param("teacherId") Long teacherId,
                                @org.apache.ibatis.annotations.Param("studentId") Long studentId);

}
