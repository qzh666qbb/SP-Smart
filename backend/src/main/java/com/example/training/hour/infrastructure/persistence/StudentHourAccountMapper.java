package com.example.training.hour.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentHourAccountMapper extends BaseMapper<StudentHourAccountEntity> {
    
    @Select("SELECT * FROM student_hour_account WHERE student_id = #{studentId} FOR UPDATE")
    StudentHourAccountEntity selectForUpdate(@Param("studentId") Long studentId);

    @Update("""
        UPDATE student_hour_account 
        SET remaining_hours = #{entity.remainingHours}, 
            total_consumed = #{entity.totalConsumed},
            total_recharged = #{entity.totalRecharged},
            total_refunded = #{entity.totalRefunded},
            paid_hours = #{entity.paidHours},
            gift_hours = #{entity.giftHours},
            experience_hours = #{entity.experienceHours},
            activity_hours = #{entity.activityHours},
            version = version + 1,
            updated_at = NOW()
        WHERE id = #{entity.id} AND version = #{entity.version}
        """)
    int updateWithVersion(@Param("entity") StudentHourAccountEntity entity);
}
