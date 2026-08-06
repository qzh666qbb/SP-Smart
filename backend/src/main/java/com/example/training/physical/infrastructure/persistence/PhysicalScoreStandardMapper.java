package com.example.training.physical.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PhysicalScoreStandardMapper extends BaseMapper<PhysicalScoreStandardEntity> {

    @Select("SELECT * FROM physical_score_standard WHERE item_id = #{itemId} AND gender = #{gender} AND age_group = #{ageGroup} ORDER BY min_value ASC")
    List<PhysicalScoreStandardEntity> findByItemIdAndGenderAndAgeGroup(
            @Param("itemId") Long itemId,
            @Param("gender") String gender,
            @Param("ageGroup") String ageGroup);
}
