package com.example.training.course.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.training.course.infrastructure.persistence.ClassGroupStudentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ClassGroupStudentMapper extends BaseMapper<ClassGroupStudentEntity> {
    
    @Insert("<script>" +
            "INSERT INTO class_group_student (class_group_id, student_id, joined_at, status, created_by, created_at, updated_at) " +
            "VALUES <foreach collection='studentIds' item='studentId' separator=','> " +
            "(#{classGroupId}, #{studentId}, #{joinedAt}, #{status}, #{createdBy}, NOW(), NOW()) " +
            "</foreach>" +
            " ON DUPLICATE KEY UPDATE status = '#{status}', left_at = NULL, updated_at = NOW()" +
            "</script>")
    int batchInsert(@Param("classGroupId") Long classGroupId,
                    @Param("studentIds") List<Long> studentIds,
                    @Param("joinedAt") LocalDateTime joinedAt,
                    @Param("status") String status,
                    @Param("createdBy") Long createdBy);
    
    @Update("UPDATE class_group_student SET status = #{status}, left_at = #{leftAt}, updated_at = NOW() " +
            "WHERE class_group_id = #{classGroupId} AND student_id IN <foreach collection='studentIds' item='studentId' open='(' separator=',' close=')' /> " +
            "AND status = 'ACTIVE'")
    int batchUpdateStatus(@Param("classGroupId") Long classGroupId,
                          @Param("studentIds") List<Long> studentIds,
                          @Param("status") String status,
                          @Param("leftAt") LocalDateTime leftAt);
}
