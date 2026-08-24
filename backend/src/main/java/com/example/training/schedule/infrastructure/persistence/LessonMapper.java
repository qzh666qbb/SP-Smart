package com.example.training.schedule.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LessonMapper extends BaseMapper<LessonEntity> {

    @Update("""
        UPDATE lesson 
        SET status = #{entity.status},
            completed_at = #{entity.completedAt},
            completed_by = #{entity.completedBy},
            cancel_reason = #{entity.cancelReason},
            updated_at = NOW(),
            updated_by = #{entity.updatedBy},
            version = version + 1
        WHERE id = #{entity.id} AND version = #{entity.version}
        """)
    int updateWithVersion(@Param("entity") LessonEntity entity);
}
