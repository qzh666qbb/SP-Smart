package com.example.training.schedule.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface LessonStudentMapper extends BaseMapper<LessonStudentEntity> {

    @Update("""
            UPDATE lesson_student ls
            INNER JOIN lesson l ON ls.lesson_id = l.id
            SET ls.attendance_status = 'ABSENT'
            WHERE ls.attendance_status = 'PENDING'
              AND l.status != 'CANCELLED'
              AND l.end_time IS NOT NULL
              AND TIMESTAMP(l.lesson_date, l.end_time) < #{threshold}
            """)
    int markAbsentForCompletedLessons(@Param("threshold") LocalDateTime threshold);
}
